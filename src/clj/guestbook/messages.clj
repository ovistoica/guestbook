;---
; Excerpted from "Web Development with Clojure, Third Edition",
; published by The Pragmatic Bookshelf.
; Copyrights apply to this code. It may not be used to create training material,
; courses, books, articles, and the like. Contact us if you are in doubt.
; We make no guarantees that this code is fit for any purpose.
; Visit http://www.pragmaticprogrammer.com/titles/dswdcloj3 for more book information.
;---
(ns guestbook.messages
  (:require
    [guestbook.db.core :as db]
    [guestbook.validation :refer [validate-message]]))


;
(defn message-list []
  {:messages (vec (db/get-messages))})
;

;
(defn save-message! [{:keys [login]} message]
  (if-let [errors (validate-message message)]
    (throw (ex-info "Message is invalid"
                    {:guestbook/error-id :validation
                     :errors             errors}))
    (db/save-message! (assoc message :author login))))
;

(defn messages-by-author [author]
  {:messages (vec (db/get-messages-by-author {:author author}))})