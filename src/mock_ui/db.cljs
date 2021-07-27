(ns mock-ui.db
 (:require [re-frame.core :refer [reg-cofx]]
           [mock-ui.util :as util]))


(def default-db {:modal-visible? false})

(reg-cofx
 ::current-user
 (fn [cofx _]
   (assoc cofx :current-user (util/get-current-user-map))))
