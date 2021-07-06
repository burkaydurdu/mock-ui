(ns mock-ui.subs
  (:require
   [re-frame.core :refer [reg-sub]]))

(reg-sub
 ::active-panel
 (fn [db _]
  (:active-panel db)))

(reg-sub
  ::current-user
  (fn [db _]
    (:current-user db)))

(reg-sub
  ::token
  :<- [::current-user]
  (fn [current-user _]
    (:token current-user)))

(reg-sub
  ::alert
  (fn [db _]
    (:alert db)))

(reg-sub
  ::create-form
  (fn [db _]
    (:create-form db)))

(reg-sub
  ::modal-form-type
  :<- [::create-form]
  (fn [create-form _]
    (:type create-form)))

(reg-sub
  ::form-workspace
  :<- [::create-form]
  (fn [create-form _]
    (:workspace create-form)))

(reg-sub
  ::workspaces
  (fn [db _]
    (:workspaces db)))

(def requests [{:id "11" :path "shop/live" :method "PUT"}
               {:id "12" :path "shop/dog" :method "GET"}])

(reg-sub
  ::requests
  (fn [db _]
    (:requests db)))

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
  ::response-code
  :<- [::response]
  (fn [response _]
    (:code response)))

(reg-sub
  ::response-type
  :<- [::response]
  (fn [response _]
    (:type response)))

(reg-sub
  ::modal-visible?
  (fn [db]
    (:modal-visible? db)))
