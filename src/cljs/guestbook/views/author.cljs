;---
; Excerpted from "Web Development with Clojure, Third Edition",
; published by The Pragmatic Bookshelf.
; Copyrights apply to this code. It may not be used to create training material,
; courses, books, articles, and the like. Contact us if you are in doubt.
; We make no guarantees that this code is fit for any purpose.
; Visit http://www.pragmaticprogrammer.com/titles/dswdcloj3 for more book information.
;---
(ns guestbook.views.author
  (:require
    [re-frame.core :as rf]
    [guestbook.messages :as messages]))

;
(def author-controllers
  [{:parameters {:path [:user]}
    :start (fn [{{:keys [user]} :path}]
             (rf/dispatch [:messages/load-by-author user]))}])


(defn author [{{{:keys [user]} :path} :parameters}]
  (let [messages (rf/subscribe [:messages/list])]
    (fn [{{{:keys [user]} :path} :parameters}]
      [:div.content>div.columns.is-centered>div.column.is-two-thirds
       [:div.columns>div.column
        [:h3 "Messages By " user]
        (if @(rf/subscribe [:messages/loading?])
          [messages/message-list-placeholder]
          [messages/message-list messages])]])))