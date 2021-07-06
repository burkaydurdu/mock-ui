(ns mock-ui.db
 (:require [re-frame.core :refer [reg-cofx]]
           [mock-ui.util :as util]))

(def response-headers [{:key "name" :val "burkay"}
                       {:key "surname" :val "Durdu"}
                       {:key "age" :val "34"}])

(def default-db
  {:name "Mock API"
   :modal-visible? false
   :response {:headers response-headers}})

(reg-cofx
 ::current-user
 (fn [cofx _]
   (assoc cofx :current-user (util/get-current-user-map))))
