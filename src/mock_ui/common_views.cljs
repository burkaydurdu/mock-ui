(ns mock-ui.common-views
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [mock-ui.subs :as subs]
            [mock-ui.events :as events]
            [mock-ui.util :as util]))

(defn- alert-box-body-view []
  (let [alert @(subscribe [::subs/alert])
        box-class (if (:error? alert)
                    "text-red-700 bg-red-100"
                    "text-green-700 bg-green-100")]
    [:div.px-4.py-3.m-2.leading-normal.rounded-lg.fixed.top-0.right-0.z-50
     {:class box-class}
     [:p.font-bold (:header alert)]
     [:p (:message alert)]]))

(defn- alert-box []
    (r/create-class
      {:component-did-mount #(util/sleep (fn [] (dispatch [::events/reset :alert])) 3000)
       :reagent-render (fn [] [alert-box-body-view])}))

(defn alert []
  (when @(subscribe [::subs/alert])
    [alert-box]))
