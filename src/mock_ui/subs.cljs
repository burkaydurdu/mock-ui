(ns mock-ui.subs
  (:require
   [re-frame.core :refer [reg-sub]]))

(reg-sub
 ::name
 (fn [db]
   (:name db)))

(reg-sub
 ::active-panel
 (fn [db _]
   (:active-panel db)))

;;Mocky
;;
;;

(def workspaces [{:id "1" :name "facebook"}
                 {:id "2" :name "twitter"}
                 {:id "3" :name "Instagram"}])

(reg-sub
  ::workspaces
  (fn [_ _]
    workspaces))
    ;(:workspaces db)))

(def requests [{:id "11" :path "shop/live" :method "PUT"}
               {:id "12" :path "shop/dog" :method "GET"}])

(reg-sub
  ::requests
  (fn [_ _]
    requests))
    ;;(:requests db))

(def responses [{:id "111"
                 :headers {:x-fre "radar"
                           :t-tek "sebro"}
                 :body "{\"name\": \"Hasan\"}"
                 :mimeType "JSON"
                 :code 200}
                {:id "112"
                 :headers {:x-tfre "Toradars"
                           :t-tek "sebro"}
                 :body "<name>Burkay</name>"
                 :mimeType "XML"
                 :code 500}])

(reg-sub
  ::responses
  (fn [_ _]
    responses))
    ;; (:responses db)

(reg-sub ::response :response)

(reg-sub
  ::response-header
  :<- [::response]
  (fn [response _]
    (:headers response)))

(reg-sub
  ::response-body
  :<- [::response]
  (fn [response _]
    (:body response)))

(reg-sub
  ::modal-visible?
  (fn [db]
    (:modal-visible? db)))
