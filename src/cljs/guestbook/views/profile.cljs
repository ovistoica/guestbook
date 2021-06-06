;---
; Excerpted from "Web Development with Clojure, Third Edition",
; published by The Pragmatic Bookshelf.
; Copyrights apply to this code. It may not be used to create training material,
; courses, books, articles, and the like. Contact us if you are in doubt.
; We make no guarantees that this code is fit for any purpose.
; Visit http://www.pragmaticprogrammer.com/titles/dswdcloj3 for more book information.
;---
(ns guestbook.views.profile
  (:require
   [guestbook.components :refer [text-input textarea-input image image-uploader]]
   [reagent.core :as r]
   [re-frame.core :as rf]))

(rf/reg-sub
 :profile/changes
 (fn [db _]
   (get db :profile/changes)))

;
(rf/reg-sub
 :profile/media
 (fn [db _]
   (get db :profile/media)))

(rf/reg-sub
 :profile/changed?
 :<- [:profile/changes]
 :<- [:profile/media]
 (fn [[changes media] _]
   (not
    (and
     (empty? changes)
     (empty? media)))))

(rf/reg-sub
 :profile/field-changed?
 :<- [:profile/changes]
 :<- [:profile/media]
 (fn [[changes media] [_ k]]
   (or
    (contains? changes k)
    (contains? media k))))

(rf/reg-sub
 :profile/field
 :<- [:profile/changes]
 :<- [:auth/user]
 :<- [:profile/media]
 (fn [[changes {:keys [profile]} media] [_ k default]]
   (or
    (when-let [file (get media k)] (js/URL.createObjectURL file))
    (get changes k)
    (get profile k)
    default)))

(rf/reg-event-db
 :profile/save-media
 (fn [db [_ k v]]
   (update db
           :profile/media
           (if (nil? v)
             #(dissoc % k)
             #(assoc % k v)))))
;

(rf/reg-sub
 :profile/profile
 :<- [:profile/changes]
 :<- [:auth/user]
 (fn [[changes {:keys [profile]}] _]
   (merge
    profile
    changes)))

(rf/reg-event-db
 :profile/save-change
 (fn [db [_ k v]]
   (update db
           :profile/changes
           (if (nil? v)
             #(dissoc % k)
             #(assoc % k v)))))

;
(rf/reg-event-fx
 :profile/set-profile
 (fn [_ [_ profile files]]
   (if (some some? (vals files))
     {:ajax/upload-media!
      {:url     "/api/my-account/media/upload"
       :files   files
       :handler (fn [response]
                  (rf/dispatch
                   [:profile/set-profile
                    (merge profile
                           (select-keys response (:files-uploaded response)))]))}}
     {:ajax/post
      {:url           "/api/my-account/set-profile"
       :params        {:profile profile}
       :success-event [:profile/handle-set-profile profile]}})))

(rf/reg-event-db
 :profile/handle-set-profile
 (fn [db [_ profile]]
   (-> db
       (assoc-in [:auth/user :profile] profile)
       (dissoc
        :profile/media
        :profile/changes))))
;

(defn display-name []
  (r/with-let [k :display-name
               value (rf/subscribe [:profile/field k ""])]
    [:div.field
     [:label.label {:for k} "Display Name"
      (when @(rf/subscribe [:profile/field-changed? k])
        " (Changed)")]
     [:div.field.has-addons
      [:div.control.is-expanded
       [text-input {:value   value
                    :on-save #(rf/dispatch [:profile/save-change k %])}]]
      [:div.control>button.button.is-danger
       {:disabled (not @(rf/subscribe [:profile/field-changed? k]))
        :on-click #(rf/dispatch [:profile/save-change k nil])} "Reset"]]]))

(defn bio []
  (r/with-let [k :bio
               value (rf/subscribe [:profile/field k ""])]
    [:div.field
     [:label.label {:for k} "Bio"
      (when @(rf/subscribe [:profile/field-changed? k])
        " (Changed)")]
     [:div.control {:style {:margin-bottom "0.5em"}}
      [textarea-input {:value   value
                       :on-save #(rf/dispatch [:profile/save-change k %])}]]
     [:div.control>button.button.is-danger
      {:disabled (not @(rf/subscribe [:profile/field-changed? k]))
       :on-click #(rf/dispatch [:profile/save-change k nil])} "Reset"]]))

;
(defn avatar []
  (r/with-let [k :avatar
               url (rf/subscribe [:profile/field k ""])]
    [:<>
     [:h3 "Avatar"
      (when @(rf/subscribe [:profile/field-changed? k])
        " (Changed)")]
     [image @url 128 128]
     [:div.field.is-grouped
      [:div.control
       [image-uploader
        #(rf/dispatch [:profile/save-media k %])
        "Choose an Avatar..."]]
      [:div.control>button.button.is-danger
       {:disabled (not @(rf/subscribe [:profile/field-changed? k]))
        :on-click #(rf/dispatch [:profile/save-media k nil])}
       "Reset Avatar"]]]))

(defn banner []
  (r/with-let [k :banner
               url (rf/subscribe [:profile/field k ""])]
    [:<>
     [:h3 "Banner"
      (when @(rf/subscribe [:profile/field-changed? k])
        " (Changed)")]
     [image @url 1200 400]
     [:div.field.is-grouped
      [:div.control
       [image-uploader
        #(rf/dispatch [:profile/save-media k %])
        "Choose a Banner..."]]
      [:div.control>button.button.is-danger
       {:disabled (not @(rf/subscribe [:profile/field-changed? k]))
        :on-click #(rf/dispatch [:profile/save-media k nil])}
       "Reset Banner"]]]))
;

(def profile-controllers
  [{:start (fn [_] (println "Entering Profile Page"))
    :stop  (fn [_] (println "Leaving Profile Page"))}])


(defn change-password []
  (let [fields (r/atom {})
        errors (r/atom {})
        success (r/atom {})]
    (letfn [(password-field [id label]
              (r/with-let [v (r/cursor fields [id])
                           e (r/cursor errors [id])]
                [:div.label
                 [:label.label {:for id} label]

                 [:input.input {:id id
                                :type :password
                                :value @v
                                :on-change #(reset! v (.. % -target -value))}]
                 (when-let [message @e]
                   [:p.help.is-danger message])]))
            (change-password! []
                            (let [{:keys [new-password 
                                          :confirm-password] :as params} @fields]))]
      (fn []
        [:<>
         [:h3 "Change password"]
         [password-field :old-password "Current password"]
         [password-field :new-password "New password"]
         [password-field :confirm-password "New password (confirm)"]
         [:div.field
          (when-let [message (:server @errors)]
            [:p.message.is-danger message])
          (when-let [message (:message @success)]
            [:p.message.is-success message])
          [:button.button
           {:on-click
            (fn [_]
              (change-password!))}
           "Change password"]]]))))









(defn profile [_]
  (if-let [{:keys [login created_at profile]} @(rf/subscribe [:auth/user])]
    [:div.content
     [:h1 "My Account"
      (str "  <@" login ">")]
     [:p (str "Joined: " (.toString created_at))]
     [display-name]
     [bio]
     [avatar]
     [banner]
     (let [disabled? (not @(rf/subscribe [:profile/changed?]))]
       [:button.button.is-primary.is-large
        {:style    {:width      "100%"
                    :position   :sticky
                    :bottom     10
                    :visibility (if disabled?
                                  :hidden
                                  :visible)}
         :on-click
         #(rf/dispatch [:profile/set-profile
                        @(rf/subscribe [:profile/profile])
                        @(rf/subscribe [:profile/media])])
         :disabled disabled?}
        "Update Profile"])]
    [:div.content
     [:div {:style {:width "100%"}}
      [:progress.progress.is-dark {:max 100} "30%"]]]))
;