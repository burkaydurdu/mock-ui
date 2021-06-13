(ns mock-ui.effects
  (:require [re-frame.core :refer [reg-fx]]
            [mock-ui.routes :refer [navigate!]]))

(reg-fx
  :navigate
  (fn [handler]
    (navigate! handler)))
