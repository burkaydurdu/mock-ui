(ns mock-ui.events
  (:require
   [re-frame.core :refer [reg-event-fx reg-event-db inject-cofx]]
   [mock-ui.db :as db]
   [mock-ui.util :as util]
   [day8.re-frame.http-fx]
   [day8.re-frame.tracing :refer-macros [fn-traced]]))

(reg-event-fx
 ::initialize-db
 [(inject-cofx ::db/current-user)]
 (fn-traced [{:keys [_ current-user]}]
            {:db (assoc db/default-db :current-user current-user)}))

(reg-event-fx
  ::navigate
  (fn-traced [_ [_ handler]]
             {:navigate handler}))

;; [TODO] Look at this and should be improve that
(reg-event-fx
  ::set-active-panel
  (fn-traced [{:keys [db]} [_ active-panel]]
             (let [current-user (:current-user db)
                   not-valid?   (and (#{:sign-in-panel :sign-up-panel} active-panel)
                                     current-user)
                   active-panel (if not-valid?
                                  :dashboard-panel
                                  active-panel)
                   body         {:db (assoc db :active-panel active-panel)}]
               (if not-valid?
                 (assoc body :change-uri! "/dashboard")
                 body))))

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
  ::alert
  (fn [db [_ header message error?]]
    (assoc db :alert {:header header
                      :message message
                      :error? (true? error?)})))

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
          headers (reduce #(conj %1 (hash-map (-> %2 :key keyword) (:val %2)))
                          {}
                          (:headers response))
          request-body {:code (:code response)
                        :headers headers
                        :mimeType (:type response)
                        :body (:body response)}]
      (println request-body)
      {})))

(reg-event-db
  ::edit-response
  (fn [db [_ response]]
    (let [headers (reduce #(conj %1 (hash-map :key (key %2) :val (val %2)))
                          []
                          (:headers response))]
      (merge db {:modal-visible? true
                 :response {:headers headers
                            :type (:mimeType response)
                            :code (:code response)
                            :body (:body response)}}))))

(reg-event-fx
  ::sign-up
  (fn [{:keys [db]} _]
    (let [form (-> db :sign-up :form)]
      (if (and (util/contains-many? form :email :name :password :password-confirm)
               (= (:password form) (:password-confirm form)))
        {:http-xhrio (merge (util/create-request-map :post "/users/sign-up"
                                                     ::sign-up-result-ok
                                                     ::sign-up-result-fail)
                            {:params (dissoc form :password-confirm)})}
        {:db (assoc-in db [:errors :sign-up] true)}))))

(reg-event-fx
  ::sign-up-result-ok
  (constantly {:dispatch [::alert "Success" "Register is successful"]}))

(reg-event-fx
  ::sign-up-result-fail
  (constantly {:dispatch [::alert "Error" "Register is unsuccessful, please try again" true]}))

(reg-event-fx
  ::sign-in
  (fn [{:keys [db]} _]
    (let [form (-> db :sign-in :form)]
      (if (util/contains-many? form :email :password)
        {:http-xhrio (merge (util/create-request-map :post "/users/sign-in"
                                                     ::sign-in-result-ok
                                                     ::sign-in-result-fail)
                            {:params form})}
        {:db (assoc-in db [:errors :sign-in] true)}))))

(reg-event-fx
  ::sign-in-result-ok
  (fn [{:keys [db]} [_ result]]
    {:db (assoc db :current-user result)
     :set-user! result
     :dispatch [::alert "Success" "Login is successful"]
     :change-uri! "/dashboard"}))

(reg-event-fx
  ::sign-in-result-fail
  (constantly {:dispatch [::alert "Error" "Login is unsuccessful, please try again" true]}))


(reg-event-fx
  ::get-workspaces
  (fn [_ _]
    {:http-xhrio (util/create-request-map :get "/workspaces"
                                          ::get-workspaces-result-ok)}))

(reg-event-db
  ::get-workspaces-result-ok
  (fn [db [_ result]]
    (assoc db :workspaces result)))

(reg-event-fx
  ::create-workspace
  (fn [{:keys [db]} _]
    (when-let [workspace (-> db :create-form :workspace :name)]
      {:http-xhrio (merge (util/create-request-map :post "/workspaces"
                                                   ::create-workspace-result-ok)
                          {:params {:name workspace}})})))

(reg-event-db
  ::create-workspace-result-ok
  (fn [db [_ result]]
    (-> db
        (update :workspaces conj result)
        (dissoc :create-form))))

(reg-event-fx
  ::delete-workspace
  (fn [_ [_ id]]
    (when id
      {:http-xhrio (util/create-request-map :delete (str "/workspaces/" id)
                                            ::delete-workspace-result-ok)})))

(reg-event-db
  ::delete-workspace-result-ok
  (fn [db [_ result]]
    (assoc db :workspaces (filterv #(not= (:id result) (:id %)) (:workspaces db)))))

(reg-event-fx
  ::get-requests
  (fn [{:keys [db]} [_ workspace-id]]
    {:db         (assoc-in db [:selected :workspace] workspace-id)
     :http-xhrio (util/create-request-map :get (str "/requests/workspace/" workspace-id)
                                          ::get-requests-result-ok)}))

(reg-event-db
  ::get-requests-result-ok
  (fn [db [_ result]]
    (assoc db :requests result)))

(reg-event-fx
  ::create-request
  (fn [{:keys [db]} _]
    (let [form      (-> db :create-form :request)
          workspace (-> db :selected :workspace)]
      (when (and workspace (util/contains-many? form :method :path))
       {:http-xhrio (merge (util/create-request-map :post "/requests"
                                                    ::create-request-result-ok)
                           {:params {:method      (:method form)
                                     :path        (:path form)
                                     :workspaceId workspace}})}))))

(reg-event-db
  ::create-request-result-ok
  (fn [db [_ result]]
    (-> db
        (update :requests conj result)
        (dissoc :create-form))))

(reg-event-fx
  ::update-response
  (fn [{:keys [db]} _]
    {}))

(reg-event-db
  ::update-response-result-ok
  (fn [db _]
    db))

(reg-event-fx
  ::delete-response
  (fn [{:keys [db]} [_ id]]
    {}))

(reg-event-db
  ::delete-response-result-ok
  (fn [db _]
    db))
