(ns clojure.core.typed.init
  (:require [clojure.core.typed.current-impl :as impl]
            [clojure.core.typed.profiling :as p]
            [clojure.java.io :as io]))

(defonce ^:private attempted-loading? (atom false))
(defonce ^:private successfully-loaded? (atom false))
(defonce ^:private cljs-loaded? (atom false))

(defonce ^:private cljs-present? (atom false))

(defn loaded? []
  @successfully-loaded?)

(defn has-cljs-loaded? []
  @cljs-loaded?)

(defn load-impl 
  ([] (load-impl false))
  ([cljs?]
  (cond 
    (and @attempted-loading?
         (not @successfully-loaded?))
    (throw (Exception. 
             (str "There was previously an unrecoverable internal error while loading core.typed." 
                  " Please restart your process.")))

    (and @successfully-loaded? @attempted-loading?
         (if cljs?
           @cljs-loaded?
           true))
    nil

    :else
    (do
      (try
        (reset! attempted-loading? true)
        (require '[clojure.core.typed.utils]
                 '[clojure.core.typed.type-rep]
                 '[clojure.core.typed.type-ctors]
                 '[clojure.core.typed.filter-rep]
                 '[clojure.core.typed.filter-ops]
                 '[clojure.core.typed.subst]
                 '[clojure.core.typed.path-rep]
                 '[clojure.core.typed.object-rep]
                 '[clojure.core.typed.fold-rep]
                 '[clojure.core.typed.fold-default]
                 '[clojure.core.typed.parse-unparse]
                 '[clojure.core.typed.lex-env]
                 '[clojure.core.typed.var-env]
                 '[clojure.core.typed.parse-unparse]
                 '[clojure.core.typed.current-impl]
                 '[clojure.core.typed.env]
                 '[clojure.core.typed.dvar-env]
                 '[clojure.core.typed.datatype-ancestor-env]
                 '[clojure.core.typed.datatype-env]
                 '[clojure.core.typed.protocol-env]
                 '[clojure.core.typed.method-override-env]
                 '[clojure.core.typed.ctor-override-env]
                 '[clojure.core.typed.method-return-nilables]
                 '[clojure.core.typed.method-param-nilables]
                 '[clojure.core.typed.declared-kind-env]
                 '[clojure.core.typed.name-env]
                 '[clojure.core.typed.rclass-env]
                 '[clojure.core.typed.mm-env]
                 '[clojure.core.typed.constant-type]
                 '[clojure.core.typed.parse-unparse]
                 '[clojure.core.typed.frees]
                 '[clojure.core.typed.free-ops]
                 '[clojure.core.typed.cs-gen]
                 '[clojure.core.typed.trans]
                 '[clojure.core.typed.inst]
                 '[clojure.core.typed.subtype]
                 '[clojure.core.typed.array-ops]
                 '[clojure.core.typed.check]
                 '[clojure.core.typed.infer-vars]
                 '[clojure.core.typed.reset-caches]
                 '[clojure.core.typed.check-ns-common]
                 '[clojure.core.typed.check-ns-clj]
                 '[clojure.core.typed.check-form-common]
                 '[clojure.core.typed.check-form-clj]
                 '[clojure.core.typed.statistics]
                 '[clojure.core.typed.load1]

                 '[clojure.core.typed.parse-ast]
                 '[clojure.core.typed.file-mapping]
                 '[clojure.core.typed.base-env]
                 '[clojure.core.typed.ns-deps]
                 '[clojure.core.typed.reset-env]
                 '[clojure.core.typed.tvar-env]
                 '[clojure.core.typed.tvar-bnds]
                 '[clojure.core.typed.rclass-ancestor-env]
                 '[clojure.core.typed.all-envs]
                 '[clojure.reflect])
        (when cljs?
          (do
            (println "Found ClojureScript, loading ...")
            (flush)
            (require
              '[cljs.analyzer]
              '[cljs.util]
              '[clojure.core.typed.collect-cljs]
              '[clojure.core.typed.check-cljs]
              '[clojure.core.typed.jsnominal-env]
              '[clojure.core.typed.base-env-cljs]
              '[clojure.core.typed.check-ns-cljs]
              '[clojure.core.typed.check-form-cljs]
              '[clojure.core.typed.check-ns-cljs]
              '[clojure.core.typed.base-env-helper-cljs])
            (reset! cljs-present? true)
            (reset! cljs-loaded? true)
            (println "Finished loading ClojureScript")
            (flush)))
        (catch Exception e
          (reset! successfully-loaded? false)
          (throw e)))
      (reset! successfully-loaded? true)
      (println "Building core.typed base environments ...")
      (flush)
      ;(impl/with-clojure-impl
      ;  ((impl/v 'clojure.core.typed.reset-env/reset-envs!)))
      (impl/with-clojure-impl
        ((impl/v 'clojure.core.typed.reset-env/load-core-envs!)))
      (when cljs?
        (impl/with-cljs-impl
          ;; FIXME should be load-core-envs!
          ((impl/v 'clojure.core.typed.reset-env/reset-envs!) cljs?)))
      (println "Finished building base environments")
      (flush)
      nil))))
