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
  (query-database [db sql])
  (create-entity [db entity params]))

(extend-protocol Database
  duct.database.sql.Boundary
  (query-database [{:keys [spec]} sql]
    (try
      (let [result (jdbc/query spec sql)]
        [nil result])
      (catch Exception e
        ["Something went wrong with the query", nil]))))

  (comment

  (create-entity [{:keys [spec]} entity params]
    (try
      [nil, (-> (jdbc/insert! db entity params) ffirst val)]
      (catch Exception e
        (let [[text table column] (re-find #"UNIQUE constraint failed:\s(\w*)\.(\w*)" (.getMessage e))]
          [{(keyword column) "Has already been taken"} nil]))))

  )

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

(comment
(defn validate-params [params schema]
  (st/validate params schema {:strip true})))

; Prep Keys
(defmethod ig/prep-key :tjape.handler.generic/list [_ config]
  (merge
   {:db (ig/ref :duct.database/sql)
    :view (ig/ref :tjape.view.generic/list)} config))

(defmethod ig/prep-key :tjape.handler.generic/detail [_ config]
  (merge
   {:db (ig/ref :duct.database/sql)} config))

(defmethod ig/prep-key :tjape.handler.generic/new [_ config]
  (merge
   {:view (ig/ref :tjape.view.generic/new)} config))

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

(defmethod ig/init-key :tjape.handler.generic/new [_ {:keys [fields view]}]
  (fn [{:keys [flash]}]
    (let [data flash]
      [::response/ok (view fields data)])))

(comment

(defmethod ig/init-key :tjape.handler.generic/post [_ {:keys [db schema success-route error-route]}]
  (fn [{:keys [params]}]
    (let [[errors params] (validate-param params schema)
          [errors result] (if-not errors (create-entity db params) [errors nil])]
    (if errors
      (-> (ring/redirect error-route)
          (assoc :flash (assoc params :errors errors)))
      (do
        (ring/redirect success-route))))))
)
