;---
; Excerpted from "Web Development with Clojure, Third Edition",
; published by The Pragmatic Bookshelf.
; Copyrights apply to this code. It may not be used to create training material,
; courses, books, articles, and the like. Contact us if you are in doubt.
; We make no guarantees that this code is fit for any purpose.
; Visit http://www.pragmaticprogrammer.com/titles/dswdcloj3 for more book information.
;---
(defproject guestbook "0.1.0-SNAPSHOT"

  :description "FIXME: write description"
  :url "http://example.com/FIXME"

  :dependencies [[ch.qos.logback/logback-classic "1.2.3"]
                 [cheshire "5.10.0"]
                 [clojure.java-time "0.3.2"]
                 [com.h2database/h2 "1.4.200"]
                 [conman "0.9.1"]
                 [cprop "0.1.17"]
                 [expound "0.8.7"]
                 [funcool/struct "1.4.0"]
                 [luminus-http-kit "0.1.9"]
                 [luminus-migrations "0.7.1"]
                 [luminus-transit "0.1.2"]
                 [luminus/ring-ttl-session "0.3.3"]
                 [markdown-clj "1.10.5"]
                 [lein-kibit "0.1.6"]
                 [metosin/muuntaja "0.6.7"]
                 [metosin/reitit "0.5.10"]
                 [metosin/ring-http-response "0.9.1"]
                 [metosin/ring-swagger-ui "2.2.10"]
                 [metosin/jsonista "0.3.3"]
                 [com.cognitect/transit-clj "1.0.324"]
                 [mount "0.1.16"]
                 [nrepl "0.8.3"]
                 [org.clojure/clojure "1.10.1"]
                 [org.clojure/tools.cli "1.0.194"]
                 [org.clojure/tools.logging "1.1.0"]
                 [org.webjars.npm/bulma "0.9.1"]
                 [org.webjars.npm/material-icons "0.3.1"]
                 [org.webjars/webjars-locator "0.40"]
                 [ring-webjars "0.2.0"]
                 [ring/ring-core "1.8.2"]
                 [ring/ring-defaults "0.3.2"]
                 [reagent "1.0.0"]
                 [re-frame "1.1.2"]
                 [org.clojure/core.async "1.2.603"]
                 [cljs-ajax "0.8.1"]
                 [org.clojure/clojurescript "1.10.764" :scope "provided"]
                 [com.google.javascript/closure-compiler-unshaded "v20200830" :scope "provided"]
                 [org.clojure/google-closure-library "0.0-20191016-6ae1f72f" :scope "provided"]
                 [thheller/shadow-cljs "2.11.14" :scope "provided"]
                 [selmer "1.12.31"]
                 [buddy "2.0.0"]
                 [binaryage/devtools "1.0.3"]
                 [com.taoensso/sente "1.16.0"]
                 [org.postgresql/postgresql "42.2.18"]
                 [day8.re-frame/re-frame-10x "1.0.2"]]

  :min-lein-version "2.0.0"

  :source-paths ["src/clj" "src/cljc" "src/cljs"]
  :test-paths ["test/clj"]
  :clean-targets ^{:protect false}
  [:target-path "target/cljsbuild"]
  :resource-paths ["resources" "target/cljsbuild"]
  :target-path "target/%s/"
  :main ^:skip-aot guestbook.core
  
  :plugins [[lein-shadow "0.2.0"]]

  :shadow-cljs {:nrepl  {:port 7002}
                :builds {:app  {:target     :browser
                                :output-dir "target/cljsbuild/public/js"
                                :asset-path "/js"
                                :modules    {:app {:entries [guestbook.app]}}
                                :dev        {:closure-defines {"re_frame.trace.trace_enabled_QMARK_" true}}
                                :devtools   {:preloads  [day8.re-frame-10x.preload]
                                             :watch-dir "resources/public"}}}
                :lein   true}
  :npm-dev-deps [[xmlhttprequest "1.8.0"]]


  :profiles
  {:uberjar       {:omit-source    true
                   :aot            :all
                   :uberjar-name   "guestbook.jar"
                   :source-paths   ["env/prod/clj" "env/prod/cljc" "env/prod/cljs"]
                   :prep-tasks ["compile" ["shadow" "release" "app"]]
                   :resource-paths ["env/prod/resources"]}

   :dev           [:project/dev :profiles/dev]
   :test          [:project/dev :project/test :profiles/test]

   :project/dev   {:jvm-opts       ["-Dconf=dev-config.edn"]
                   :dependencies [[binaryage/devtools "1.0.2"]
                                  [cider/piggieback "0.5.0"]
                                  [pjstadig/humane-test-output "0.10.0"]
                                  [prone "2020-01-17"]
                                  [ring/ring-devel "1.8.1"]
                                  [ring/ring-mock "0.4.0"]]
                   :plugins        [[com.jakemccrary/lein-test-refresh "0.24.1"]
                                    [jonase/eastwood "0.3.5"]]

                   :source-paths ["env/dev/clj" "env/dev/cljs" "test/cljs"]
                   :resource-paths ["env/dev/resources"]
                   :repl-options   {:init-ns user
                                    :timeout 120000}
                   :injections     [(require 'pjstadig.humane-test-output)
                                    (pjstadig.humane-test-output/activate!)]}
   :project/test  {:jvm-opts       ["-Dconf=test-config.edn"]
                   :resource-paths ["env/test/resources"]}
   :profiles/dev  {}
   :profiles/test {}})
