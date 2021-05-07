;---
; Excerpted from "Web Development with Clojure, Third Edition",
; published by The Pragmatic Bookshelf.
; Copyrights apply to this code. It may not be used to create training material,
; courses, books, articles, and the like. Contact us if you are in doubt.
; We make no guarantees that this code is fit for any purpose.
; Visit http://www.pragmaticprogrammer.com/titles/dswdcloj3 for more book information.
;---
(ns guestbook.env
  (:require [clojure.tools.logging :as log]))

;
(def defaults
  {:init
   (fn []
     (log/info "\n-=[guestbook started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[guestbook has shut down successfully]=-"))
   :middleware identity})
;
