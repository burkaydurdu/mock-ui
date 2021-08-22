(ns mock-ui.events
  (:require
   [re-frame.core :refer [reg-event-fx reg-event-db inject-cofx]]
   [mock-ui.db :as db]
   [mock-ui.util :as util]
   [mock-ui.helper :as helper]
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

(reg-event-fx
  ::set-active-panel
  (fn-traced [{:keys [db]} [_ active-panel]]
             (let [current-user (:current-user db)
                   case-1 (when (#{:sign-in-panel :sign-up-panel} active-panel)
                            (if current-user :dashboard-panel active-panel))
                   case-2 (when (= :dashboard-panel active-panel)
                           (if current-user active-panel :sign-in-panel))
                   valid? (= (or case-1 case-2) active-panel)
                   body   {:db (assoc db :active-panel active-panel)}]
               (if valid?
                 body
                 {:change-uri! (if case-1 "/dashboard" "/sign-in")}))))

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

(reg-event-db
  ::edit-response
  (fn [db [_ response]]
    (let [headers (reduce #(conj %1 (hash-map :key (key %2) :val (val %2)))
                          []
                          (:headers response))]
      (merge db {:modal-visible? true
                 :response {:id      (:id response)
                            :headers headers
                            :type    (:mimeType response)
                            :code    (:code response)
                            :body    (:body response)}}))))

(reg-event-fx
  ::sign-up
  (fn [{:keys [db]} _]
    (let [form (-> db :sign-up :form)]
      (if (and (util/contains-many? form :email :name :password :password-confirm)
               (= (:password form) (:password-confirm form)))
        {:http-xhrio (merge (helper/create-request-map :post "/users/sign-up"
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
        {:http-xhrio (merge (helper/create-request-map :post "/users/sign-in"
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
  ::route
  (fn [_ [_ route]]
    {:change-uri! route}))

(reg-event-fx
  ::get-workspaces
  (fn [_ _]
    {:http-xhrio (helper/create-request-map :get "/workspaces"
                                            ::get-workspaces-result-ok)}))

(reg-event-db
  ::get-workspaces-result-ok
  (fn [db [_ result]]
    (assoc db :workspaces result)))

(reg-event-fx
  ::create-workspace
  (fn [{:keys [db]} _]
    (when-let [workspace (-> db :create-form :workspace :name)]
      {:http-xhrio (merge (helper/create-request-map :post "/workspaces"
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
      {:http-xhrio (helper/create-request-map :delete (str "/workspaces/" id)
                                              ::delete-workspace-result-ok)})))

(reg-event-db
  ::delete-workspace-result-ok
  (fn [db [_ result]]
    (assoc db :workspaces (filterv #(not= (:id result) (:id %)) (:workspaces db)))))

(reg-event-fx
  ::get-requests
  (fn [{:keys [db]} [_ workspace-id]]
    {:db         (-> db
                     (assoc-in [:selected :workspace] workspace-id)
                     (dissoc :request :requests :responses))
     :http-xhrio (helper/create-request-map :get (str "/requests/workspace/" workspace-id)
                                            ::get-requests-result-ok)
     :dispatch   [::disconnect-ws]}))

(reg-event-db
  ::get-requests-result-ok
  (fn [db [_ result]]
    (assoc db :requests result)))

(reg-event-fx
  ::get-responses
  (fn [{:keys [db]} [_ request]]
    (when-let [id (:id request)]
     {:db         (assoc db :request request)
      :http-xhrio (helper/create-request-map :get (str "/responses/request/" id)
                                             ::get-responses-result-ok)
      :dispatch-n [[::disconnect-ws]
                   [::connect-ws]]})))

(reg-event-db
  ::get-responses-result-ok
  (fn [db [_ result]]
    (assoc db :responses result)))

(reg-event-fx
  ::create-request
  (fn [{:keys [db]} _]
    (let [form      (-> db :create-form :request)
          workspace (-> db :selected :workspace)]
      (when (and workspace (util/contains-many? form :method :path))
       {:http-xhrio (merge (helper/create-request-map :post "/requests"
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
  ::update-request
  (fn [{:keys [db]} _]
    (when-let [request (:request db)]
      {:http-xhrio (merge (helper/create-request-map :put "/requests"
                                                     ::update-request-result-ok)
                          {:params request})})))

(reg-event-fx
  ::update-request-result-ok
  (fn [{:keys [db]} [_ result]]
    {:dispatch [::alert "Success" "Updated request"]
     :db (assoc db :requests (util/find-and-all-update :id (:id result) result (:requests db)))}))

(reg-event-fx
  ::delete-request
  (fn [{:keys [db]} _]
    (when-let [id (-> db :request :id)]
      {:http-xhrio (helper/create-request-map :delete (str "/requests/" id)
                                              ::delete-request-result-ok)})))

(reg-event-db
  ::delete-request-result-ok
  (fn [db [_ result]]
    (-> db
        (assoc :requests (filterv #(not= (:id result) (:id %)) (:requests db)))
        (dissoc :request :responses))))

(reg-event-fx
  ::create-response
  (fn [{:keys [db]} _]
    (let [response     (:response db)
          headers      (reduce #(conj %1 (hash-map (-> %2 :key keyword) (:val %2)))
                               {}
                               (:headers response))
          request-id   (-> db :request :id)
          request-body {:code      (:code response)
                        :headers   headers
                        :mimeType  (:type response)
                        :body      (:body response)
                        :requestId request-id}]
      (when request-id
        {:http-xhrio (merge (helper/create-request-map :post "/responses"
                                                       ::create-response-result-ok
                                                       ::create-response-result-fail)
                            {:params request-body})}))))

(reg-event-db
  ::create-response-result-ok
  (fn [db [_ result]]
    (-> db
        (update :responses conj result)
        (assoc :modal-visible? false))))

(reg-event-fx
  ::create-response-result-fail
  (constantly {:dispatch [::alert "Error" "Something went wrong" true]}))

(reg-event-fx
  ::update-response
  (fn [{:keys [db]} _]
    (let [response     (:response db)
          headers      (reduce #(conj %1 (hash-map (-> %2 :key keyword) (:val %2)))
                               {}
                               (:headers response))
          request-body {:id        (:id response)
                        :code      (:code response)
                        :headers   headers
                        :mimeType  (:type response)
                        :body      (:body response)}]
      {:http-xhrio (merge (helper/create-request-map :put "/responses"
                                                     ::update-response-result-ok
                                                     ::update-response-result-fail)
                          {:params request-body})})))

(reg-event-db
  ::update-response-result-ok
  (fn [db [_ result]]
    (assoc db :responses (util/find-and-all-update :id (:id result) result (:responses db))
              :modal-visible? false)))

(reg-event-fx
  ::update-response-result-fail
  (constantly {:dispatch [::alert "Something went wrong" true]}))

(reg-event-fx
  ::delete-response
  (fn [_ [_ id]]
    {:http-xhrio (helper/create-request-map :delete (str "/responses/" id)
                                            ::delete-response-result-ok)}))
(reg-event-db
  ::delete-response-result-ok
  (fn [db [_ result]]
    (assoc db :responses (filterv #(not= (:id result) (:id %)) (:responses db)))))

(reg-event-fx
  ::logout
  (fn [{:keys [db]} _]
    (when (:current-user db)
      {:http-xhrio (helper/create-request-map :post "/users/logout"
                                              ::logout-result-ok)})))

(reg-event-fx
  ::logout-result-ok
  (fn [{:keys [db]} _]
    {:db (dissoc db :current-user)
     :remove-user! "user"
     :change-uri! "/sign-in"}))

(reg-event-db
  ::remove-dashboard-data
  (fn [db _]
    (dissoc db :workspaces :selected :requests :request :responses)))

(reg-event-fx
  ::connect-ws
  (fn [{:keys [db]} _]
    (let [user-id      (-> db :current-user :id)
          workspace-id (-> db :selected :workspace) ;; [TODO] Change data structure
          request-id   (-> db :request :id)]
      {:connect-ws (helper/socket-connection-url user-id workspace-id request-id)})))

(reg-event-fx
  ::disconnect-ws
  (fn [{:keys [db]} _]
    (when-let [socket (:socket db)]
     {:disconnect-ws socket})))

(reg-event-fx
  ::initial-route
  (fn [{:keys [db]} _]
    (if (:current-user db)
      {:change-uri! "/dashboard"}
      {:change-uri! "/sign-in"})))
