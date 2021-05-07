(ns guestbook.app
  (:require
    [guestbook.core :as core]))

;; ignore println statements in prod
(set! *print-fn* (fn [& -]))

(core/init!)
