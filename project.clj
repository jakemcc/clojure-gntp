(defproject jakemcc/clojure-gntp "0.1.1"
  :description "Implementation of Growl GNTP"
  :url "https://github.com/jakemcc/clojure-gntp"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]]
  :plugins [[lein-autoexpect "0.2.1-SNAPSHOT"]]
  :lein-release {:deploy-via :clojars}
  :profiles {:dev {:dependencies [[expectations "1.4.10"]]}})
