;---
; Excerpted from "Web Development with Clojure, Third Edition",
; published by The Pragmatic Bookshelf.
; Copyrights apply to this code. It may not be used to create training material,
; courses, books, articles, and the like. Contact us if you are in doubt.
; We make no guarantees that this code is fit for any purpose.
; Visit http://www.pragmaticprogrammer.com/titles/dswdcloj3 for more book information.
;---
;
(ns guestbook.routes.services
  (:require
    [reitit.swagger :as swagger]
    [reitit.swagger-ui :as swagger-ui]
    [reitit.ring.coercion :as coercion]
    [reitit.coercion.spec :as spec-coercion]
    [reitit.ring.middleware.muuntaja :as muuntaja]
    [reitit.ring.middleware.exception :as exception]
    [reitit.ring.middleware.multipart :as multipart]
    [reitit.ring.middleware.parameters :as parameters]
    [guestbook.messages :as msg]
    [guestbook.middleware :as middleware]
    [spec-tools.data-spec :as ds]
    [ring.util.http-response :as response]
    [guestbook.middleware.formats :as formats]
    [guestbook.auth :as auth]
    [guestbook.auth.ring :refer [wrap-authorized get-roles-from-match]]
    [guestbook.handlers.auth :as auth-handlers]
    [guestbook.handlers.messages :as msg-handlers]
    [guestbook.handlers.profile :as profile-handlers]
    [guestbook.author :as author]
    [clojure.tools.logging :as log]))

(defn service-routes []
  ["/api"
   ;
   {:middleware [;; query-params & form-params
                 parameters/parameters-middleware
                 ;; content-negotiation
                 muuntaja/format-negotiate-middleware
                 ;; encoding response body
                 muuntaja/format-response-middleware
                 ;; exception handling
                 exception/exception-middleware
                 ;; decoding request body
                 muuntaja/format-request-middleware
                 ;; coercing response bodys
                 coercion/coerce-response-middleware
                 ;; coercing request parameters
                 coercion/coerce-request-middleware
                 ;; multipart params
                 multipart/multipart-middleware

                 ;; authentication and check roles
                 (fn [handler]
                   (wrap-authorized
                     handler
                     (fn handle-unauthorized [req]
                       (let [route-roles (get-roles-from-match req)]
                         (log/debug "Rules for route: " (:uri req) route-roles)
                         (log/debug "User is unauthorized!"
                                    (-> req
                                        :session
                                        :identity
                                        :roles))
                         (response/forbidden
                           {:message
                            (str "User must have one of the following roles: " route-roles)})))))]
    :muuntaja   formats/instance
    :coercion   spec-coercion/coercion
    :swagger    {:id ::api}}
   ;

   ["" {:no-doc      true
        ::auth/roles (auth/roles :swagger/swagger)}
    ["/swagger.json"
     {:get (swagger/create-swagger-handler)}]
    ["/swagger-ui*"
     {:get (swagger-ui/create-swagger-ui-handler
             {:url "/api/swagger.json"})}]]

   ["/login"
    {::auth/roles (auth/roles :auth/login)
     :post        {:parameters {:body {:login    string?
                                       :password string?}}
                   :responses  {200 {:body {:identity {:login      string?
                                                       :created_at inst?}}}
                                401 {:body {:message string?}}}
                   :handler    auth-handlers/login}}]

   ["/register"
    {::auth/roles (auth/roles :account/register)
     :post        {:parameters {:body {:login    string?
                                       :password string?
                                       :confirm  string?}}
                   :responses  {200 {:body {:message string?}}
                                400 {:body {:message string?}}
                                409 {:body {:message string?}}}
                   :handler    auth-handlers/register}}]

   ["/logout"
    {::auth/roles (auth/roles :auth/logout)
     :post        {:handler (fn [_]
                              (->
                                (response/ok)
                                (assoc :session nil)))}}]

   ["/session"
    {
     ::auth/roles (auth/roles :session/get)
     :get
                  {:responses {200 {:body {:session
                                           {:identity
                                            (ds/maybe {:login      string?
                                                       :created_at inst?})}}}}
                   :handler   auth-handlers/get-session}}]

   ["/messages"
    {::auth/roles (auth/roles :messages/list)}
    ["" {:get {:summary   "Get all messages"
               :responses {200 {:body [{:id        pos-int?
                                        :name      string?
                                        :message   string?
                                        :timestamp inst?}]}}

               :handler   (fn [_]
                            (response/ok (msg/message-list)))}}]

    ["/by/:author"
     {:get {:parameters {:path {:author string?}}
            :summary    "Get all messages by the author"
            :responses
                        {200 {:body {:messages [{:id        pos-int?
                                                 :name      string?
                                                 :message   string?
                                                 :timestamp inst?}]}}}
            :handler
                        (fn [{{{:keys [author]} :path} :parameters}]
                          (response/ok (msg/messages-by-author author)))}}]]

   ["/message"
    {::auth/roles (auth/roles :message/create!)
     :post        {:parameters {:body
                                {:name    string?
                                 :message string?}}
                   :summary    "Create a message"

                   :responses  {200 {:body map?}
                                400 {:body map?}
                                500 {:errors map?}}

                   :handler    msg-handlers/create-message}}]
   ["/author/:login"
    {::auth/roles (auth/roles :author/get)
     :get         {:parameters {:path {:login string?}}
                   :responses  {200 {:body map?}
                                500 {:errors map?}}
                   :handler    (fn [{{{:keys [login]} :path} :parameters}]
                                 (response/ok (author/get-author login)))}}]
   ["/my-account"
    ["/set-profile"
     {::auth/roles (auth/roles :account/set-profile!)
      :post        {:parameters {:body {:profile map?}}
                    :responses  {200 {:body map?}
                                 500 {:body map?}}
                    :handler    profile-handlers/set-profile}}]]])

