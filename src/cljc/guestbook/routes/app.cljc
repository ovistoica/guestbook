;
(ns guestbook.routes.app
  (:require
    #?@(:clj  [[guestbook.layout :as layout]
               [guestbook.middleware :as middleware]]
        :cljs [[spec-tools.data-spec :as ds]
               [guestbook.views.home :as home]
               [guestbook.views.profile :as profile]
               [guestbook.views.post :as post]
               [guestbook.views.author :as author]])))
#?(:clj
   (defn home-page [request]
     (layout/render
       request
       "home.html")))

(defn app-routes []
  [""
   #?(:clj {:middleware [middleware/wrap-csrf]
            :get        home-page})
   ["/"
    (merge
     {:name ::home}
     #?(:cljs
        {:parameters {:query {(ds/opt :post) pos-int?}}
         :controllers home/home-controllers
         :view        #'home/home}))]
   ["/post/:post"
    (merge
     {:name ::post}
     #?(:cljs {:parameters {:path {:post pos-int?}}
               :controllers post/post-controllers
               :view #'post/post-page}))]
   
   ["/user/:user"
    (merge
     {:name ::author}
     #?(:cljs {:parameters {:query {(ds/opt :post) pos-int?}
                            :path {:user string?}}
               :controllers author/author-controllers
               :view        #'author/author}))]
   ["/my-account/edit-profile"
    (merge
     {:name ::profile}
     #?(:cljs
        {:controllers profile/profile-controllers
         :view        #'profile/profile}))]])
;