;---
; Excerpted from "Web Development with Clojure, Third Edition",
; published by The Pragmatic Bookshelf.
; Copyrights apply to this code. It may not be used to create training material,
; courses, books, articles, and the like. Contact us if you are in doubt.
; We make no guarantees that this code is fit for any purpose.
; Visit http://www.pragmaticprogrammer.com/titles/dswdcloj3 for more book information.
;---
(ns guestbook.ajax
  (:require
    [ajax.core :refer [GET POST]]
    [re-frame.core :as rf]))

(rf/reg-fx
  :ajax/get
  (fn [{:keys [url success-event error-event success-path]}]
    (GET url
         (cond-> {:headers {"Accept" "application/transit+json"}}
                 success-event (assoc :handler
                                      #(rf/dispatch
                                         (conj success-event
                                               (if success-path
                                                 (get-in % success-path)
                                                 %))))
                 error-event (assoc :error-handler
                                    #(rf/dispatch
                                       (conj error-event %)))))))

(rf/reg-fx
  :ajax/post
  (fn [{:keys [url success-event error-event success-path params]}]
    (POST url
          (cond-> {:headers {"Accept" "application/transit+json"}}
                  params (assoc :params params)
                  success-event (assoc :handler
                                       #(rf/dispatch
                                          (conj success-event
                                                (if success-path
                                                  (get-in % success-path)
                                                  %))))
                  error-event (assoc :error-handler
                                     #(rf/dispatch (conj error-event %)))))))

(rf/reg-fx
  :ajax/upload-media!
  (fn [{:keys [url success-event files handler]}]
    (let [form-data (js/FormData.)]
      (doseq [[k v] files]
        (when (some? v)
          (.append form-data (name k) v)))
      (POST url {:headers {"Accept" "application/transit+json"}
                 :body    form-data
                 :handler handler}))))