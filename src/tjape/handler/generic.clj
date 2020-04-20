(ns tjape.handler.generic
  (:require [ataraxy.response :as response]
            [clojure.java.jdbc :as jdbc]
            duct.database.sql
            [integrant.core :as ig]
            [ring.util.response :as ring]
            [tjape.view.generic :as view])
  (:use clojure.pprint))

; Boundaries
(defprotocol Database
  (query-database [db sql]))

(extend-protocol Database
  duct.database.sql.Boundary
  (query-database [{:keys [spec]} sql]
    (try
      (let [result (jdbc/query spec sql)]
        [nil result])
      (catch Exception e
        ["Something went wrong with the query", nil]))))

(defn convert-sql-params [sql params]
  "This function attempts to convert a set of parameters. It takes a really simple approach
   in that it tries to convert a specific parameter to an integer and if this fails assumes it
   is a string."
  (let [sql-keys (rest sql)
        sql-params (map (fn [key]
                          (try
                            (Integer/parseInt (params key))
                            (catch Exception e
                              (params key)))) sql-keys)]

    [nil (concat [(first sql)] (vec sql-params))]))

; Prep Keys
(defmethod ig/prep-key :tjape.handler.generic/list [_ config]
  (merge
   {:db (ig/ref :duct.database/sql)
    :view (ig/ref :tjape.view.generic/list)} config))

(defmethod ig/prep-key :tjape.handler.generic/detail [_ config]
  (merge
   {:db (ig/ref :duct.database/sql)} config))

; Initialise Keys
(defmethod ig/init-key :tjape.handler.generic/list [_ {:keys [db sql view]}]
  (fn [request]
    (let [[error params] [nil (merge (request :route-params) (request :params))]
          [error sql-query] (convert-sql-params sql params)
          [error result] (if-not error (query-database db sql-query) [error nil])]
      (if error
        [::response/ok (view/error error)]
        [::response/ok (view result)]))))

(defmethod ig/init-key :tjape.handler.generic/detail [_ {:keys [db sql view]}]
  (fn [{:keys [route-params]}]
    (let [[error sql-query] (convert-sql-params sql route-params)
          [error result] (if-not error (query-database db sql-query) [error nil])]
      (if error
        [::response/ok (view/error error)]
        [::response/ok (view (first result))]))))
