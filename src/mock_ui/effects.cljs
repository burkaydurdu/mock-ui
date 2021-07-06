(ns mock-ui.effects
  (:require [re-frame.core :refer [reg-fx]]
            [mock-ui.routes :refer [navigate! set-uri-token!]]
            [mock-ui.util :as util]))

(reg-fx
  :navigate
  (fn [handler]
    (navigate! handler)))

(reg-fx
 :set-user!
 (fn [user]
   (util/set-item! "user" user)))

(reg-fx
 :remove-user!
 (fn [k]
   (util/remove-item! k)))

(reg-fx
 :change-uri!
 (fn [uri]
   (set-uri-token! uri)))
