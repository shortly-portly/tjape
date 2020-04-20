(ns tjape.view.bootstrap
  (:require [hiccup.page :refer [html5 include-css include-js]]
            [hiccup.form :refer [form-to label text-field submit-button]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]))

(defn layout [content]
  (html5
   [:head
    (include-js "/js/jquery-3.3.1.slim.min.js"
                "/js/popper.min.js"
                "/js/bootstrap.js")
    (include-css "/css/bootstrap.css")]

   [:div.container
    content]))

(defn jumbotron [content]
  [:div.jumbotron content])

(defn form [post-url data content]
  (form-to [:post post-url]
           (anti-forgery-field)
           content))

(defn text-input [label name data]
  (let [valid-class (if (= nil (get-in data [:errors name]))
                  nil
                  "is-invalid")]
  [:div.form-group
   [:label label]
   [:input.form-control {:type "text" :id name :name name :value (name data)  :class valid-class} ]
   (if-let [error-text (get-in data [:errors name])]
     [:div.invalid-feedback error-text])]))

(defn submit-btn [label]
  (submit-button {:class "btn btn-primary"} label))

(defn table [headers content]
  [:table.table
   [:thead
    [:tr
     (for [title headers]
       [:th title])]]
   [:tbody
    content]])
