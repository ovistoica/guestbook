;---
; Excerpted from "Web Development with Clojure, Third Edition",
; published by The Pragmatic Bookshelf.
; Copyrights apply to this code. It may not be used to create training material,
; courses, books, articles, and the like. Contact us if you are in doubt.
; We make no guarantees that this code is fit for any purpose.
; Visit http://www.pragmaticprogrammer.com/titles/dswdcloj3 for more book information.
;---
(ns user
  "Userspace functions you can run by default in your local REPL."
  (:require
   [guestbook.config :refer [env]]
   [clojure.pprint]
   [clojure.spec.alpha :as s]
   [expound.alpha :as expound]
   [mount.core :as mount]
   [guestbook.core :refer [start-app]]
   [guestbook.db.core]
   [guestbook.auth :as auth]
   [conman.core :as conman]
   [luminus-migrations.core :as migrations]))



(alter-var-root #'s/*explain-out* (constantly expound/printer))

(add-tap (bound-fn* clojure.pprint/pprint))

;
(defn start
  "Starts application.
  You'll usually want to run this on startup."
  []
  (mount/start-without #'guestbook.core/repl-server))

(defn stop
  "Stops application."
  []
  (mount/stop-except #'guestbook.core/repl-server))

(defn restart
  "Restarts application."
  []
  (stop)
  (start))

(defn restart-db
  "Restarts database."
  []
  (mount/stop #'guestbook.db.core/*db*)
  (mount/start #'guestbook.db.core/*db*)
  (binding [*ns* (the-ns 'guestbook.db.core)]
    (conman/bind-connection guestbook.db.core/*db* "sql/queries.sql")))
;

(defn reset-db
  "Resets database."
  []
  (migrations/migrate ["reset"] (select-keys env [:database-url])))

(defn migrate
  "Migrates database up for all outstanding migrations."
  []
  (migrations/migrate ["migrate"] (select-keys env [:database-url])))

(defn rollback
  "Rollback latest database migration."
  []
  (migrations/migrate ["rollback"] (select-keys env [:database-url])))

(defn create-migration
  "Create a new up and down migration file with a timestamp and `name`."
  [name]
  (migrations/create name (select-keys env [:database-url])))



(comment

  (auth/create-user! "testuser" "testpass")
  (auth/authenticate-user "testurer" "testpass")

  (migrate)

  )