(ns mock-ui.views
  (:require
   [re-frame.core :refer [subscribe dispatch]]
   [mock-ui.routes :as routes]
   [mock-ui.logistration-views :as logistration-views]
   [mock-ui.dashboard-views :as dashboard-views]
   [mock-ui.navigation-views :as navigation]
   [mock-ui.common-views :as common-views]
   [mock-ui.events :as events]
   [mock-ui.subs :as subs]))

;; ---
;; Reset Password views
;; ---
(defmethod routes/panels :reset-password-panel [] [logistration-views/reset-password-view])
;; ---
;; Send Reset Password views
;; ---
(defmethod routes/panels :send-reset-password-panel [] [logistration-views/send-reset-password-view])
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
;; Home
;; ---
(defmethod routes/panels :home-panel [] (dispatch [::events/initial-route]))


;; ---
;; Main
;; There is a routing logic
;; ----
(defn main-panel []
  (let [active-panel (subscribe [::subs/active-panel])]
    [:div.h-full.md:overflow-hidden
     [common-views/alert]
     [navigation/navigation-bar]
     (routes/panels @active-panel)]))
