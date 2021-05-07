;---
; Excerpted from "Web Development with Clojure, Third Edition",
; published by The Pragmatic Bookshelf.
; Copyrights apply to this code. It may not be used to create training material,
; courses, books, articles, and the like. Contact us if you are in doubt.
; We make no guarantees that this code is fit for any purpose.
; Visit http://www.pragmaticprogrammer.com/titles/dswdcloj3 for more book information.
;---
(ns guestbook.handler
  (:require
    [guestbook.middleware :as middleware]
    [guestbook.layout :refer [error-page]]
    [guestbook.routes.home :refer [home-routes]]
    [guestbook.routes.services :refer [service-routes]]
    [reitit.ring :as ring]
    [ring.middleware.content-type :refer [wrap-content-type]]
    [ring.middleware.webjars :refer [wrap-webjars]]
    [guestbook.env :refer [defaults]]
    [mount.core :as mount]))

;
(mount/defstate init-app
  :start ((or (:init defaults) (fn [])))
  :stop  ((or (:stop defaults) (fn []))))
;

;
(mount/defstate app-routes
  :start
  (ring/ring-handler
    (ring/router
      [(home-routes)
       (service-routes)])
    (ring/routes
      (ring/create-resource-handler
        {:path "/"})
      (wrap-content-type
        (wrap-webjars (constantly nil)))
      (ring/create-default-handler
        {:not-found
         (constantly 
          (error-page
           {:status 404, :title "404 - Page not found"}))
         :method-not-allowed
         (constantly
          (error-page
           {:status 405, :title "405 - Not allowed"}))
         :not-acceptable
         (constantly
          (error-page
           {:status 406, :title "406 - Not acceptable"}))}))))
;

(defn app []
  (middleware/wrap-base #'app-routes))
