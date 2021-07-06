(ns mock-ui.views
  (:require
   [re-frame.core :as re-frame]
   [mock-ui.routes :as routes]
   [mock-ui.logistration-views :as logistration-views]
   [mock-ui.dashboard-views :as dashboard-views]
   [mock-ui.common-views :as common-views]
   [mock-ui.subs :as subs]))

;; ---
;; Sign-Up views
;; ---
(defmethod routes/panels :sign-up-panel [] [logistration-views/sign-up-view])
;; ---
;; Sign-In views
;; ---
(defmethod routes/panels :sign-in-panel [] [logistration-views/sign-in-view])
;; ---
;; Sign-In views
;; ---
(defmethod routes/panels :dashboard-panel [] [dashboard-views/dashboard-view])

;; ---
;; Main
;; There is a routing logic
;; ----
(defn main-panel []
  (let [active-panel (re-frame/subscribe [::subs/active-panel])]
    [:div.h-full
     [common-views/alert]
     (routes/panels @active-panel)]))
