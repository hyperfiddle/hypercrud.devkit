(ns sample-app.load
  (:require [clojure.java.io :as io]
            [datomic.api :as d]
            [hypercrud.server.datomic.root-init :as root-init]))


(defn load-samples-blog [uri]
  (let [schema (-> (io/resource "samples-blog/schema.edn") slurp read-string)
        data (-> (io/resource "samples-blog/data.edn") slurp read-string)
        conn (d/connect uri)]
    @(d/transact conn schema)
    @(d/transact conn data)))

(defn initialize [transactor-uri]
  (let [samples-blog-uri (str transactor-uri "samples-blog")]
    (println "Loading samples-blog")
    (when-not (d/create-database samples-blog-uri)
      (throw (Error. "samples-blog db already exists")))
    (load-samples-blog samples-blog-uri))

  (let [root-schema (-> (io/resource "root/schema.edn") slurp read-string)
        root-data (conj (-> (io/resource "samples-blog/app-value.edn") slurp read-string)
                        ; the only reason this root db-record exists in this loader is to satisfy init-datomic
                        ; it can be deleted when hypercrud.server.db-root/root-id is
                        (root-init/generate-db-record "root"))]
    (root-init/init transactor-uri root-schema root-data)))