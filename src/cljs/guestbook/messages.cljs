(ns guestbook.messages
  (:require
   [clojure.string :as string]
   [reagent.core :as r]
   [re-frame.core :as rf]
   [reitit.frontend.easy :as rtfe]
   [guestbook.components :refer [textarea-input image md image-uploader]]
   [guestbook.validation :refer [validate-message]]
   [reagent.dom :as dom]))


(rf/reg-event-db
 :messages/set
 (fn [db [_ messages]]
   (-> db
       (assoc :messages/loading? false
              :messages/list messages))))

(rf/reg-sub
 :messages/loading?
 (fn [db _]
   (:messages/loading? db)))

(rf/reg-sub
 :messages/list
 (fn [db _]
   (:messages/list db [])))

(defn add-message? [filter-map msg]
  (every?
   (fn [[k matcher]]
     (let [v (get msg k)]
       (cond
         (set? matcher) (matcher v)
         (fn? matcher) (matcher v)
         :else (= matcher v))))
   filter-map))


(rf/reg-event-db
 :message/add
 (fn [db [_ message]]
   (if (add-message? (:messages/filter db) message)
     (update db :messages/list conj message)
     db)))

(rf/reg-event-fx
 :messages/load
 (fn [{:keys [db]} _]
   {:db       (assoc db
                     :messages/loading? true
                     :messages/list nil
                     :messages/filter nil)
    :ajax/get {:url           "/api/messages"
               :success-path  [:messages]
               :success-event [:messages/set]}}))


(rf/reg-event-fx
 :messages/load-by-author
 (fn [{:keys [db]} [_ author]]
   {:db       (-> db
                  (assoc :messages/loading? true
                         :messages/list nil
                         :messages/filter {:author author}))
    :ajax/get {:url           (str "/api/messages/by/" author)
               :success-path  [:messages]
               :success-event [:messages/set]}}))


(rf/reg-event-db
 :form/set-field
 [(rf/path :form/fields)]
 (fn [fields [_ id value]]
   (assoc fields id value)))

(rf/reg-event-db
 :form/clear-fields
 [(rf/path :form/fields)]
 (fn [_ _]
   {}))

(rf/reg-sub
 :form/fields
 (fn [db _]
   (:form/fields db)))

(rf/reg-sub
 :form/field
 :<- [:form/fields]
 (fn [fields [_ id]]
   (get fields id)))

(rf/reg-event-db
 :form/set-server-errors
 [(rf/path :form/server-errors)]
 (fn [_ [_ errors]]
   errors))

(rf/reg-sub
 :form/server-errors
 (fn [db _]
   (:form/server-errors db)))

;;Validation errors are reactively computed
(rf/reg-sub
 :form/validation-errors
 :<- [:form/fields]
 (fn [fields _]
   (validate-message fields)))

(rf/reg-sub
 :form/validation-errors?
 :<- [:form/validation-errors]
 (fn [errors _]
   (seq errors)))

(rf/reg-sub
 :form/errors
 :<- [:form/validation-errors]
 :<- [:form/server-errors]
 (fn [[validation server] _]
   (merge validation server)))

(rf/reg-sub
 :form/error
 :<- [:form/errors]
 (fn [errors [_ id]]
   (get errors id)))

(rf/reg-event-fx
 :message/send!-called-back
 (fn [_ [_ {:keys [success errors]}]]
   (if success
     {:dispatch-n [[:form/clear-fields] [:message/clear-media]]}
     {:dispatch [:form/set-server-errors errors]})))

(rf/reg-event-fx
 :message/send!
 (fn [{:keys [db]} [_ fields media]]
   (if (not-empty media)
     {:db       (dissoc db :form/server-errors)
      :ajax/upload-media! {:url "/api/my-account/media/upload"
                           :files media
                           :handler
                           (fn [response]
                             (rf/dispatch
                              [:message/send!
                               (update fields :message
                                       string/replace
                                       #"\!\[(.*)\]\((.+)\)"
                                       (fn [old alt url]
                                         (str "![" alt "]("
                                              (if-some [name ((:message/urls db) url)]
                                                (get response name)
                                                url) ")")))]))}
      :ws/send! {:message        [:message/create! fields]
                 :timeout        10000
                 :callback-event [:message/send!-called-back]}}
     {:db       (dissoc db :form/server-errors)
      :ws/send! {:message        [:message/create! fields]
                 :timeout        10000
                 :callback-event [:message/send!-called-back]}})))

(rf/reg-event-db
 :message/save-media
 (fn [db [_ img]]
   (let [url (js/URL.createObjectURL img)
         name (keyword (str "msg-" (random-uuid)))]
     (-> db
         (update-in [:form/fields :message] str "![](" url ")")
         (update :message/media (fnil assoc {}) name img)
         (update :message/urls (fnil assoc {}) url name)))))

(rf/reg-event-db
 :message/clear-media
 (fn [db _]
   (dissoc db :message/media :message/urls)))

(rf/reg-sub
 :message/media
 (fn [db _]
   (:message/media db)))


(defn message
  ([m] [message m {}])
  ([{:keys [timestamp message name author avatar id]}
    {:keys [include-link?]
     :or {include-link? true}}]
   [:article.media
    [:figure.media-left
     [image (or avatar "/img/avatar-default.png") 123 123]]
    [:div.media-content>div.content
     [:time (.toLocaleString timestamp)]
     [md message]
     (when include-link?
       [:p>a {:on-click (fn [_]
                          (let [{{:keys [name]} :data
                                 {:keys [path query]} :parameters}
                                @(rf/subscribe [:router/current-route])]
                            (rtfe/replace-state name path (assoc query :post id)))
                          (rtfe/push-state :guestbook.routes.app/post {:post id}))}
        "View Post"])
     [:p " - " name
      " <"
      (if author
        [:a {:href (str "/user/" author)} (str "@" author)]
        [:span.is-italic "account not found"])
      ">"]]]))

(defn message-preview [m]
  (r/with-let [expanded (r/atom false)]
    [:<>
     [:button.button.is-secondary.is-fullwidth
      {:on-click #(swap! expanded not)}
      (if @expanded
        "Hide Preview"
        "Show preview")]
     (when @expanded
       [:ul.messages
        {:style
         {:margin-left 0}}
        [:li
         [message m
          {:include-link? false?}]]])]))




(defn errors-component [id & [message]]
  (when-let [error @(rf/subscribe [:form/error id])]
    [:div.notification.is-danger (if message
                                   message
                                   (string/join error))]))


(defn reload-messages-button []
  (let [loading? (rf/subscribe [:messages/loading?])]
    [:button.button.is-info.is-fullwidth
     {:on-click #(rf/dispatch [:messages/load])
      :disabled @loading?}
     (if @loading?
       "Loading Messages"
       "Refresh Messages")]))

(defn message-list-placeholder []
  [:ul.messages
   [:li
    [:p "Loading Messages..."]
    [:div {:style {:width "10em"}}
     [:progress.progress.is-dark {:max 100} "30%"]]]])


(defn message-form []
  [:div.card
   [:div.card-header>p.card-header-title "Post Something!"]
   (let [{:keys [login profile]} @(rf/subscribe [:auth/user]) 
         display-name (:display-name profile login)]
     [:div.card-content
      [message-preview
       {:message @(rf/subscribe [:form/field :message])
        :id -1
        :timestamp (js/Date.)
        :name display-name
        :author login
        :avatar (:avatar profile)}]
      [errors-component :server-error]
      [errors-component :unauthorized "Please log in before posting."]
      [:div.field
       [:label.label {:for :name} "Name"]
       display-name]
      [:div.field 
       [:div.control 
        [image-uploader 
         #(rf/dispatch [:message/save-media %])
         "Insert an image"]]]
      [:div.field
       [:label.label {:for :message} "Message"]
       [errors-component :message]

       [textarea-input
        {:attrs {:name :message}
         :save-timeout 1000
         :value (rf/subscribe [:form/field :message])
         :on-save #(rf/dispatch [:form/set-field :message %])}]]
      [:input.button.is-primary.is-fullwidth
       {:type :submit
        :disabled @(rf/subscribe [:form/validation-errors?]) 
        :on-click #(rf/dispatch [:message/send!
                                 @(rf/subscribe [:form/fields])
                                 @(rf/subscribe [:message/media])])
        :value "comment"}]])])



(defn msg-li [m message-id]
  (r/create-class
   {:component-did-mount
    (fn [this]
      (when (= message-id (:id m))
        (.scrollIntoView (dom/dom-node this))))
    :reagent-render
    (fn [_]
      [:li
       [message m]])}))

(defn message-list
  ([messages]
   [message-list messages nil])
  ([messages message-id]
   [:ul.messages
    (for [m @messages]
      ^{:key (:timestamp m)}
      [:li
       [msg-li m message-id]])]))


