(ns guestbook.handlers.media
  (:require [clojure.java.io :as io]
            [ring.util.http-response :as response]
            [guestbook.db.core :as db]
            [guestbook.media :as media]
            [clojure.tools.logging :as log]
            [clojure.string :as string]))


(defn get-media [{{{:keys [name]} :path} :parameters}]
  (log/debug name)
  (if-let [{:keys [data type]} (db/get-file {:name name})]
    (do
      (log/debug data type)
      (-> (io/input-stream data)
          (response/ok)
          (response/content-type type)) (response/not-found))))


;(defn upload-media
;  [{{{:keys [avatar banner] :as mp} :multipart} :parameters
;    {:keys [identity] :as session}              :session :as req}]
;  (response/ok
;    {:avatar (str "/api/media/"
;                  (media/insert-image-returning-name
;                    (assoc avatar
;                      :filename
;                      (str (:login identity)
;                           "_avatar"))
;                    {:owner (:login identity)}))
;     :banner (str "/api/media/"
;                  (media/insert-image-returning-name
;                    (assoc banner
;                      :filename
;                      (str (:login identity)
;                           "_banner"))
;                    {:owner (:login identity)}))}))


(defn upload-media
  [{{mp :multipart}    :parameters
    {:keys [identity]} :session}]
  (response/ok
    (reduce-kv
      (fn [acc name {:keys [size content-type] :as file-part}]
        (cond
          (> size (* 5 1024 1024))
          (do
            (log/error "File " name
                       " exceeded max size of 5 MB. (size: " size ")")
            (update acc :failed-uploads (fnil conj []) name))
          (re-matches #"image/.*" content-type)
          (-> acc
              (update :files-uploaded conj name)
              (assoc name
                     (str "/api/media"
                          (cond
                            (= name :avatar)
                            (media/insert-image-returning-name
                              (assoc file-part
                                :filename
                                (str (:login identity) "_avatar.png"))
                              {:width  128
                               :height 128
                               :owner  (:login identity)})
                            (= name :banner)
                            (media/insert-image-returning-name
                              (assoc file-part
                                :filename
                                (str (:login identity) "_banner.png"))
                              {:width  1200
                               :height 400
                               :owner  (:login identity)})
                            :else
                            (media/insert-image-returning-name
                              (update
                                file-part
                                :filename
                                string/replace #"\.[^\.]+$" ".png")
                              {:max-width  800
                               :max-height 2000
                               :owner      (:login identity)})))))
          :else
          (do
            (log/error "Unsupported file type " content-type "for file " name)
            (update acc :failed-uploads (fnil conj []) name))))
      {:files-uploaded []}
      mp)))






