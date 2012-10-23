(ns jakemcc.clojure-gntp.other
  (:gen-class)
  (:import
   [java.net Socket]
   [java.io PrintWriter InputStreamReader BufferedReader StringWriter]
   [java.security MessageDigest]))

(defn send-line [s m] (print (str m "\r\n")))
(defn recv-line [s] (. s readLine))
(defn send-flush [s] (. s flush))

(defn gen-salt [n]
  (let [charseq (map char (concat (range 48 58) (range 97 123)))]
    (apply str (take n (repeatedly #(rand-nth charseq))))))

(defn hash-pass [password]
  (let [salt (gen-salt 8) hasher (MessageDigest/getInstance "MD5")]
    (do
      (let [
            first-part (do
                         (.reset hasher)
                         (.update hasher (.getBytes password))
                         (.update hasher (.getBytes salt))
                         (.digest hasher))
            second-part (do
                          (.reset hasher)
                          (.update hasher first-part)
                          (.digest hasher))]
        (str
         "MD5:"
         (.toString (BigInteger. 1 second-part) 16)
         "."
         (.toString (BigInteger. 1 (.getBytes salt)) 16))))))

(defrecord Growl [server port appname password])

(defmulti register (fn[this notifications icon] this))
(defmethod register :default [this notifications icon]
  (with-open [socket (Socket. (:server this) (:port this))
              out (PrintWriter. (. socket getOutputStream))
              in (BufferedReader. (InputStreamReader. (. socket getInputStream)))]
    (do
      (send-line out (str "GNTP/1.0 REGISTER NONE " (if
                                                        (:password this) (hash-pass (:password this)) "")))
      (send-line out (str "Application-Name: " (:appname this)))
      (if icon
        (send-line out (str "Application-Icon: " icon)))
      (send-line out (str "Notifications-Count: " (count notifications)))
      (send-line out "")
      (doseq [notification notifications]
        (do
          (send-line out (str "Notification-Name: " (notification :name)))
          (send-line out (str "Notification-Display-Name: " "foo"))
          (send-line out (str "Notification-Enabled: " "True"))
          (send-line out "")))
      (send-line out "")
      (send-flush out)
                                        ;      (recv-line in)
      )
    (do (. in close)
        (. out close)
        (. socket close)))) ; TODO: error check

(defmulti notify (fn[this notify title message & extra] this))
(defmethod notify :default [this notify title message & extra]
  (with-open [socket (Socket. (:server this) (:port this))
              out (PrintWriter. (. socket getOutputStream))
              out (PrintWriter. (StringWriter.))
              in (BufferedReader. (InputStreamReader. (. socket getInputStream)))]
    (do
      (send-line out (str "GNTP/1.0 NOTIFY NONE " (if
                                                      (:password this) (hash-pass (:password this)) "")))
      (send-line out (str "Application-Name: " (:appname this)))
      (send-line out (str "Notification-Name: " notify))
      (send-line out (str "Notification-Title: " title))
      (send-line out (str "Notification-Text: " message))
      (if (first extra)
        (send-line out (str "Notification-Icon: " (second extra))))
      (if (second extra)
        (send-line out (str "Notification-Callback-Target: " (second extra))))
      (send-line out "")
      (send-flush out)
                                        ;      (recv-line in)
      (do (. in close)
          (. out close)
          (. socket close))
))) ; TODO: error check

(defn growl [& args]
  (Growl.
   (nth args 0 "localhost") ; server
   (nth args 1 23053)       ; port
   (nth args 2 "clj-gntp")  ; appname
   (nth args 3 nil)))       ; password

(defn growl-notify
  [title message url icon]
  (let [g (growl)]
    (do
      (register g [{:name "clj-gntp-notify"}] nil)
      (notify g "clj-gntp-notify" title message url icon))))

(defn -main [& args]
  (if (>= (count args) 2)
    (growl-notify (nth args 0) (nth args 1) (nth args 2 nil) (nth args 2 nil))
    (println "Usage: clj-gntp [title] [message]")))