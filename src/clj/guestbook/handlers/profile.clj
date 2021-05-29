(ns guestbook.handlers.profile
  (:require [guestbook.author :as author]
            [ring.util.http-response :as response]
            [clojure.tools.logging :as log]))

(defn set-profile
  "Set user profile request handler"
  [{{{:keys [profile]} :body} :parameters
    {:keys [identity]}        :session}]
  (try
    (let [identity
          (author/set-author-profile (:login identity) profile)]
      (update (response/ok {:success true})
              :session
              assoc :identity identity))
    (catch Exception e
      (log/error e)
      (response/internal-server-error
        {:errors {:server-error ["Failed to set profile"]}}))))
