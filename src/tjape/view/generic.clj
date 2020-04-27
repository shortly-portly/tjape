(ns tjape.view.generic
  (:require [tjape.view.bootstrap :as bs]
            [integrant.core :as ig]))

(defn error [data]
  (bs/layout
  [:h1 data]))


(defmethod ig/init-key :tjape.view.generic/index [_ _]
(fn [data]
  (bs/layout
   [:div
    [:h1 "Results"]

    (bs/table ["first name" "last name" "age"]
              (for [entry data]
                [:tr
                 [:td (entry :firstname)]
                 [:td (entry :lastname)]
                 [:td (entry :age)]]))])))

(defmethod ig/init-key :tjape.view.generic/detail [_ _]
  (fn [data]
  (bs/layout
   [:div
    [:h1 "Details"]
    (for [[k v] data] [:div (str k ": " v)])])))

(defmethod ig/init-key :tjape.view.generic/list [_ _]
  (fn [data]
  (bs/layout
   [:div
    [:h1 "Results"]

    (bs/table ["auto first name" "auto last name" "auto age"]
              (for [entry data]
                [:tr
                (for [[k x] entry]
                  [:td x])]))])))

(defmethod ig/init-key :tjape.view.generic/new [_ _]
  (fn [fields data submit-url]
    (bs/layout
     [:div
      [:h1 "View"]

      (bs/form submit-url
               data
               [:div
               (for [field fields]
                 (bs/text-input (name field) field (field data)))
               (bs/submit-btn "New")])])))
