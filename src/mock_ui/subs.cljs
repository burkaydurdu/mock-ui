(ns mock-ui.subs
  (:require
   [re-frame.core :refer [reg-sub]]
   [mock-ui.util :as util]))

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
  ::selected-request
  (fn [db _]
    (let [request-id (-> db :selected :request)]
      (util/find-kv (:requests db) :id request-id))))

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

(reg-sub
  ::requests
  (fn [db _]
    (:requests db)))

(reg-sub
  ::responses
  (fn [db _]
    (:responses db)))

(reg-sub
  ::response
  :response)

(reg-sub
  ::response-id
  :<- [::response]
  (fn [response _]
    (:id response)))

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
