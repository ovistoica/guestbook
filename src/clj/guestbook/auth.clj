;---
; Excerpted from "Web Development with Clojure, Third Edition",
; published by The Pragmatic Bookshelf.
; Copyrights apply to this code. It may not be used to create training material,
; courses, books, articles, and the like. Contact us if you are in doubt.
; We make no guarantees that this code is fit for any purpose.
; Visit http://www.pragmaticprogrammer.com/titles/dswdcloj3 for more book information.
;---
(ns guestbook.auth
  (:require
   [buddy.hashers :as hashers]
   [next.jdbc :as jdbc]
   [guestbook.db.core :as db]))

(defn create-user! [login password]
  (jdbc/with-transaction [t-conn db/*db*]
    (if-not (empty? (db/get-user-for-auth* t-conn {:login login}))
      (throw (ex-info "User already exists!"
                      {:guestbook/error-id ::duplicate-user
                       :error "User already exists!"}))
      (db/create-user!* t-conn
                        {:login    login
                         :password (hashers/derive password)}))))

(defn authenticate-user [login password]
  (let [{hashed :password :as user} (db/get-user-for-auth* {:login login})]
    (when (hashers/check password hashed)
      (dissoc user :password))))


(defn change-password! [login old-password new-password]
  (jdbc/with-transaction [t-conn db/*db*]
    (let [{hashed :password} (db/get-user-for-auth* t-conn {:login login})]
      (if (hashers/check old-password hashed)
        (db/set-password-for-user!*
         t-conn
         {:login login
          :password (hashers/derive new-password)})
        (throw (ex-info "Old password must match!"
                        {:guestbook/error-id ::authentication-failure
                         :error "Passwords do not match"}))))))


(defn delete-account! [login password]
  (jdbc/with-transaction [t-conn db/*db*]
    (let [{hashed :password} (db/get-user-for-auth* t-conn {:login login})]
      (if (hashers/check password hashed)
        (db/delete-user!* t-conn {:login login})
        (throw (ex-info "Password is incorrect"
                        {:guestbook/error-id ::authentication-failure
                         :error "Password is incorrect!"}))))))

(defn identity->roles [identity]
  (cond-> #{:any}
    (some? identity) (conj :authenticated)))

;
(def roles
  {:message/create! #{:authenticated}
   :author/get #{:any}
   :account/set-profile! #{:authenticated}
   :media/get #{:any}
   :media/upload #{:authenticated}
   :auth/login #{:any}
   :auth/logout #{:any}
   :account/register #{:any}
   :session/get #{:any}
   :messages/list #{:any}
   :swagger/swagger #{:any}})

(comment



  (+ 1 1))

