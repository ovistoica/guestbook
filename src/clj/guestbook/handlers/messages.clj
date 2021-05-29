(ns guestbook.handlers.messages
  (:require [guestbook.messages :as msg]
            [ring.util.http-response :as response]))


(defn create-message
  "Create a message request handler"
  [{{params :body}     :parameters
    {:keys [identity]} :session}]
  (try
    (msg/save-message! identity params)
    (response/ok {:status :ok})
    (catch Exception e
      (let [{id     :guestbook/error-id
             errors :errors} (ex-data e)]
        (case id
          :validation
          (response/bad-request {:errors errors})
          ;;else
          (response/internal-server-error
            {:errors
             {:server-error ["Failed to save message!"]}}))))))
;