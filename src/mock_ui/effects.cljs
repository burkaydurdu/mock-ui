(ns mock-ui.effects
  (:require [re-frame.core :refer [reg-fx]]
            [mock-ui.routes :refer [navigate! set-uri-token!]]
            [mock-ui.util :as util]
            [mock-ui.socket :as socket]))

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

(reg-fx
  :connect-ws
  (fn [url]
    (socket/connect-ws url)))

(reg-fx
  :disconnect-ws
  (fn [socket]
    (socket/disconnect-ws socket)))
