(ns ^:dev/once guestbook.app
  (:require
   [guestbook.core :as core]
   [cljs.spec.alpha :as s]
   [expound.alpha :as expound]
   [devtools.core :as devtools]))

(extend-protocol IPrintWithWriter
  js/Symbol
  (-pr-writer [sym writer _]
    (-write writer (str "\"" (.toString sym) "\""))))

(set! s/*explain-out* expound/printer)

(enable-console-print!)

(println "loading env/dev/cljs/guestbook/app.cljs...")

(devtools/install!)
(core/init!)

(core/init!)