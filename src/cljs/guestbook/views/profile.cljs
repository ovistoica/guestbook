(ns guestbook.views.profile
  (:require
    [reagent.core :as r]
    [re-frame.core :as rf]))

(def profile-controllers
  [{:start (fn [_] (println "Entering Profile Page"))
    :stop  (fn [_] (println "Leaving Profile Page"))}])

(defn profile [_]
  [:div>h1 "My profile"])


