;---
; Excerpted from "Web Development with Clojure, Third Edition",
; published by The Pragmatic Bookshelf.
; Copyrights apply to this code. It may not be used to create training material,
; courses, books, articles, and the like. Contact us if you are in doubt.
; We make no guarantees that this code is fit for any purpose.
; Visit http://www.pragmaticprogrammer.com/titles/dswdcloj3 for more book information.
;---
(ns guestbook.dev-middleware
  (:require [ring.middleware.reload :refer [wrap-reload]]
            [selmer.middleware :refer [wrap-error-page]]
            [prone.middleware :refer [wrap-exceptions]]))

(defn wrap-dev [handler]
  (-> handler
      wrap-reload
      wrap-error-page
      (wrap-exceptions {:app-namespaces ['guestbook]})))
