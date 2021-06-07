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
   [guestbook.validation :refer [validate-message]]
   [clojure.tools.logging :as log]))


;
(defn message-list []
  {:messages (vec (db/get-messages))})
;

;
(defn save-message! [{{:keys [display-name]} :profile
                      :keys [login]}
                     message]
  (log/debug message login)
  (if-let [errors (validate-message message)]
    (throw (ex-info "Message is invalid"
                    {:guestbook/error-id :validation
                     :errors             errors}))
    (db/save-message! (assoc message :author login :name (or display-name login)))))
;

(defn messages-by-author [author]
  {:messages (vec (db/get-messages-by-author {:author author}))})

(defn get-message [post-id]
  (db/get-message {:id post-id}))