(ns jakemcc.clojure-gntp.gntp-test
  (:use expectations)
  (:require [jakemcc.clojure-gntp.gntp :as gntp]))

(expect "1.0" gntp/version)

(expect "NONE" gntp/encryption)

(expect "\r\n" gntp/separator)

(expect "Header-Name: value" (gntp/header "Header-Name" "value"))

(expect "True" (gntp/serialize-value true))
(expect "False" (gntp/serialize-value false))

(expect "3" (gntp/serialize-value 3))