(ns mock-ui.navigation-views
  (:require
    [re-frame.core :refer [subscribe dispatch]]
    [mock-ui.subs :as subs]
    [mock-ui.events :as events]
    [mock-ui.common-views :refer [dropdown]]))

(defn navigation-bar []
  (let [current-user @(subscribe [::subs/current-user])]
    [:nav.bg-gray-800
     [:div.flex.justify-between.items-center.px-5.h-16
      [:div
       [:h1.text-white.font-bold.text-2xl
        [:span "Puppet"]
        [:span.text-green-500 "API"]]]
      (when current-user
        [:div.text-white
         [dropdown
          {:options [{:title "Logout"
                      :class "hover:text-red-500"
                      :on-click-fn #(dispatch [::events/logout])}]
           :class   "text-white"
           :title   (:name current-user)}]])]]))
