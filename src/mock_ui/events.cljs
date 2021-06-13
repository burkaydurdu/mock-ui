(ns mock-ui.events
  (:require
   [re-frame.core :refer [reg-event-fx reg-event-db]]
   [mock-ui.db :as db]
   [mock-ui.util :as util]
   [day8.re-frame.tracing :refer-macros [fn-traced]]))

(reg-event-db
 ::initialize-db
 (fn-traced [_ _]
   db/default-db))

(reg-event-fx
  ::navigate
  (fn-traced [_ [_ handler]]
             {:navigate handler}))

(reg-event-fx
  ::set-active-panel
  (fn-traced [{:keys [db]} [_ active-panel]]
             {:db (assoc db :active-panel active-panel)}))

(reg-event-db
  ::reset-in
  (fn [db [_ ks]]
    (util/dissoc-in db ks)))

(reg-event-db
  ::reset
  (fn [db [_ k]]
    (dissoc db k)))

(reg-event-db
  ::add-data
  (fn [db [_ path value]]
    (assoc-in db path value)))

(reg-event-db
  ::update-data
  (fn [db [_ key-seq f & args]]
    (let [key-seq (if (vector? key-seq) key-seq [key-seq])]
      (apply update-in (concat [db key-seq f] args)))))

(reg-event-db
  ::update-response-header
  (fn [db [_ index key value]]
    (update-in db [:response :headers index] merge {key value})))

(reg-event-db
  ::open-modal-for-create
  (fn [db _]
      (merge db {:modal-visible? true
                 :response {:headers [] :code 100 :type "JSON"}})))

(reg-event-fx
  ::save
  (fn [{:keys [db]} _]
    (let [response (:response db)
          headers (reduce #(conj %1 (hash-map (-> %2 :key keyword) (:val %2))) {} (:headers response))
          request-body {:code (:code response)
                        :headers headers
                        :mimeType (:type response)
                        :body (:body response)}]
      (println request-body)
      {})))

