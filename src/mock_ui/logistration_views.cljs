(ns mock-ui.logistration-views
  (:require [reagent.core :as r]
            [clojure.string :as str]
            [mock-ui.events :as events]
            [re-frame.core :refer [dispatch]]))

(defn- input-view [{:keys [text type path]}]
  [:div.mb-4
   [:label.block.text-gray-700.text-sm.font-bold.mb-2
    {:for text}
    (str/capitalize text)]
   [:input.shadow.appearance-none.border.rounded.w-full.py-2.px-3.text-gray-700.leading-tight.focus:outline-none.focus:shadow-outline
    {:id          text
     :placeholder (str/capitalize text)
     :type        type
     :on-change   #(dispatch [::events/add-data path (-> % .-target .-value)])}]])

(defn- button-view [{:keys [text action]}]
  [:button.bg-blue-500.hover:bg-blue-700.text-white.font-bold.py-2.px-4.rounded.focus:outline-none.focus:shadow-outline
   {:type     "button"
    :on-click #(dispatch [action])}
   text])

(defn sign-up-view []
  (r/create-class
    {:component-will-unmount #(dispatch [::events/reset :sign-up])
     :reagent-render (fn []
                       [:div.h-full.flex.justify-center.items-center
                        [:div.w-full.max-w-xs
                         [:form.bg-white.shadow-md.rounded.px-8.pt-6.pb-8.mb-4
                          [input-view {:text "name" :type "text" :path [:sign-up :form :name]}]
                          [input-view {:text "email" :type "text" :path [:sign-up :form :email]}]
                          [input-view {:text "password" :type "password" :path [:sign-up :form :password]}]
                          [input-view {:text "password confirm" :type "password" :path [:sign-up :form :password-confirm]}]
                          [:div.flex.items-center.justify-between
                           [button-view {:text "Sign-up" :action ::events/sign-up}]]]]])}))

(defn sign-in-view []
  (r/create-class
    {:component-will-unmount #(dispatch [::events/reset :sign-in])
     :reagent-render (fn []
                       [:div.h-full.flex.justify-center.items-center
                        [:div.w-full.max-w-xs
                         [:form.bg-white.shadow-md.rounded.px-8.pt-6.pb-8.mb-4
                          [input-view {:text "email" :type "text" :path [:sign-in :form :email]}]
                          [input-view {:text "password" :type "password" :path [:sign-in :form :password]}]
                          [:div.flex.items-center.justify-between
                           [button-view {:text "Sign-in" :action ::events/sign-in}]]]]])}))
