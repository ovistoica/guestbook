(ns guestbook.handlers.auth
  (:require
    [guestbook.auth :as auth]
    [ring.util.http-response :as response])
  (:import (clojure.lang ExceptionInfo)))


(defn register
  "Create user account with login password and confirm"
  [{{{:keys [login password confirm]} :body} :parameters}]
  (if-not (= password confirm)
    (response/bad-request
      {:message "Password and Confirm do not match."})
    (try
      (auth/create-user! login password)
      (response/ok
        {:message "User registration succesful. Please login."})
      (catch ExceptionInfo e
        (if (= (:guestbook/error-id (ex-data e))
               ::auth/duplicate-user)
          (response/conflict
            {:message "Registration failed! User with login already exists."})
          (throw e))))))

(defn login
  "Login user with username and password"
  [{{{:keys [login password]} :body} :parameters
    session                          :session}]
  (if-some [user (auth/authenticate-user login password)]
    (->
      (response/ok
        {:identity user})
      (assoc :session (assoc session
                        :identity
                        user)))
    (response/unauthorized
      {:message "Incorrect login or password."})))


(defn get-session
  "Return current user session"
  [{{:keys [identity]} :session}]
  (response/ok {:session {:identity
                          (not-empty
                            (select-keys
                              identity
                              [:login :created_at :profile]))}})) ;
