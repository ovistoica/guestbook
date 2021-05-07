(ns guestbook.messages
  (:require
    [guestbook.db.core :as db]
    [guestbook.validation :refer [validate-message]]))


(defn message-list []
  {:messages (vec (db/get-messages))})

(defn save-message!
  "Save a new message to db"
  [message]
  (if-let [errors (validate-message message)]
    (throw (ex-info "Message is invalid"
                    {:guestbook/error-id :validation
                     :errors             errors}))
    (db/save-message! message)))
