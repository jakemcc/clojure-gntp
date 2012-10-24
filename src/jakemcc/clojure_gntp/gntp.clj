(ns jakemcc.clojure-gntp.gntp
  (:import [java.net Socket]
           [java.io PrintWriter InputStreamReader BufferedReader]))

(def version "1.0")
(def encryption "NONE")
(def separator "\r\n")

(def ^:private default-settings {:server "localhost"
                                 :port 23053
                                 :appname "clojure-gntp"
                                 :password nil
                                 :app "clojure-gntp"
                                 :notification "clojure-gntp-notify"})

(defmulti serialize-value type)
(defmethod serialize-value :default [x] (str x))
(defmethod serialize-value java.lang.Boolean [x] (if x "True" "False"))

(defn header [name value]
  (str name ": " (serialize-value value)))

(defn- main-header [type]
  (str "GNTP/" version " " type " " encryption " "))
(defn- ending [] ["" ""])

(defn- send-message [out entries]
  (let [message (apply str
                       (interpose separator
                                  entries))]
    (.write out message)
    (.flush out)))

(defn- application-name [name]
  (header "Application-Name" name))

(defn- read-all [in]
  (slurp in))

(defn- register [app out in]
  (send-message out
                (list* (main-header "REGISTER")
                       (application-name (:app  app))
                       (header "Notifications-Count" 1)
                       ""
                       (header "Notification-Name" (:notification app))
                       (header "Notification-Enabled" true)
                       ""
                       (ending)))
  (read-all in)
  nil)

(defn- notify [app title message out in]
  (send-message out
                (list* (main-header "NOTIFY")
                       (application-name (:app app))
                       (header "Notification-Name" (:notification app))
                       (header "Notification-Title" title)
                       (header "Notification-Text" message)
                       (ending)))
  (read-all in)
  nil)

(defn- with-socket [f]
  (with-open [socket (Socket. (:server default-settings)
                              (:port default-settings))
              out (PrintWriter. (.getOutputStream socket) true)
              in (BufferedReader. (InputStreamReader. (.getInputStream socket)))]
    (f out in)))

(defn message [title message & options]
  (let [app (merge default-settings (apply hash-map options))]
    (with-socket (partial register app))
    (with-socket (partial notify app title message))))