(ns mock-ui.dashboard-views
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [mock-ui.subs :as subs]
            [mock-ui.events :as events]
            [mock-ui.helper :as helper]
            [mock-ui.util :as util]
            [mock-ui.common-views :refer [dropdown]]
            ["pretty-print-json" :refer [prettyPrintJson]]))

(def http-methods ["GET" "POST" "PUT" "DELETE" "OPTIONS" "PATCH"])

(def http-status-codes [{:code 100 :text "Continue"}
                        {:code 101 :text "Switching Protocol"}
                        {:code 200 :text "OK"}
                        {:code 201 :text "Created"}
                        {:code 202 :text "Accepted"}
                        {:code 300 :text "Multiple Choice"}
                        {:code 400 :text "Bad Request"}
                        {:code 401 :text "Unauthorized"}
                        {:code 403 :text "Forbidden"}
                        {:code 404 :text "Not Found"}
                        {:code 409 :text "Conflict"}
                        {:code 500 :text "Internal Server Error"}
                        {:code 501 :text "Not Implemented"}
                        {:code 502 :text "Bad Gateway"}])

(def mime-types [{:type "JSON" :text "application/json"}
                 {:type "XML" :text "application/xml"}])

(defn- methods-color [method]
  (case method
    "GET"     "bg-green-300"
    "POST"    "bg-blue-300"
    "PUT"     "bg-indigo-300"
    "DELETE"  "bg-red-300"
    "OPTIONS" "bg-gray-300"
    "PATCH"   "bg-yellow-300"))

(defn- code-color [code]
  (cond
    (and (>= code 100) (< code 200)) "#a0aec0"
    (and (>= code 200) (< code 300)) "#48bb78"
    (and (>= code 300) (< code 400)) "#ecc94b"
    (and (>= code 400) (< code 500)) "#ed8936"
    (and (>= code 500) (< code 600)) "#f56565"
    :else "#4299e1"))

(def input-class-text "flex-grow shadow appearance-none border
                       text-gray-700 leading-tight
                       focus:outline-none focus:shadow-outline")

(def select-class-text "flex-grow-0 shadow appearance-none bg-gray-200
                        border border-gray-200 text-gray-700
                        leading-tight focus:outline-none
                        focus:bg-white focus:border-gray-500 cursor-pointer")

(def modal-class-text "min-w-screen h-screen animated fadeIn faster fixed
                       left-0 top-0 flex justify-center items-center inset-0
                       z-40 outline-none focus:outline-none bg-gray-500 bg-opacity-50")

(def button-class-text "text-xl border-2 rounded p-1 bg-transparent")

(defn- create-workspace-form []
  [:div.flex.m-5
   [:input
    {:class       (str input-class-text " p-2 rounded-l")
     :placeholder "Workspace"
     :on-change   #(dispatch [::events/add-data [:create-form :workspace :name] (-> % .-target .-value)])}]
   [:button
    {:class    "p-1 bg-transparent rounded-r border shadow bg-green-500 border-green-500 text-white"
     :on-click #(dispatch [::events/create-workspace])}
    "Create"]])

(defn- create-request-form []
  [:div.flex.m-5
   [:select
    {:class (str select-class-text " p-1 rounded-l")
     :on-change #(dispatch [::events/add-data [:create-form :request :method] (-> % .-target .-value)])}
    [:option
     {:disabled true
      :selected true}
     "Method"]
    (for [method http-methods]
      ^{:key method}
      [:option method])]
   [:input
    {:class       (str input-class-text " p-2")
     :placeholder "Path"
     :on-change   #(dispatch [::events/add-data [:create-form :request :path] (-> % .-target .-value)])}]
   [:button
    {:class    "p-1 bg-transparent rounded-r border shadow bg-green-500 border-green-500 text-white"
     :on-click #(dispatch [::events/create-request])}
    "Create"]])

(defn- create-modal-form []
  (r/create-class
    {:component-will-unmount #(dispatch [::events/reset :create-form])
     :reagent-render (fn [modal-type]
                        [:div
                         {:class modal-class-text}
                         [:div.w-full.max-w-lg.p-5.relative.mx-auto.my-auto.rounded-xl.shadow-lg.bg-white
                          [:i.far.fa-times-circle.text-xl.absolute.top-2.right-2.text-red-500.cursor-pointer
                            {:on-click #(dispatch [::events/reset :create-form])}]
                          (if (= :workspace modal-type)
                            [create-workspace-form]
                            [create-request-form])]])}))

(defn- workspace-list-view []
  [:div
   [:p.text-2xl "Workspaces"]
   (for [workspace @(subscribe [::subs/workspaces])]
     ^{:key (:id workspace)}
     [:div.w-full.flex.justify-between.py-1.cursor-pointer
      {:on-click #(do
                    (.stopPropagation %)
                    (dispatch [::events/get-requests (:id workspace)]))}
      [:p (:name workspace)]
      [dropdown
       {:options [{:title "Delete"
                   :class "hover:text-red-500"
                   :on-click-fn #(dispatch [::events/delete-workspace (:id workspace)])}]}]])
   [:button
    {:class "text-sm text-green-500"
     :on-click #(dispatch [::events/add-data [:create-form :type] :workspace])}
    "Add +"]])

(defn- request-list-view []
  [:div
   [:p.text-2xl "Requests"]
   (for [request @(subscribe [::subs/requests])]
     ^{:key (:id request)}
     [:div.w-full.flex.py-1.cursor-pointer
      {:on-click #(dispatch [::events/get-responses request])}
      [:p
       {:class (str "rounded-l py-1 px-2 " (methods-color (:method request)))}
       (str (:method request))]
      [:p.bg-green-100.rounded-r.py-1.px-2.w-full (:path request)]])
   [:button
    {:class "text-sm text-green-500"
     :on-click #(dispatch [::events/add-data [:create-form :type] :request])}
    "Add +"]])

(defn- active-status-code []
  [:div.mb-2
   [:p.text-xl.mb-1 "Status Code"]
   [:select.block
     {:class     (str select-class-text " py-3 px-4 pr-8 rounded")
      :on-change #(dispatch [::events/add-data [:response :code] (-> % .-target .-value js/parseInt)])}
     (for [status-code http-status-codes]
       ^{:key (str "active-status-code-" (:code status-code))}
       [:option
        {:selected (= @(subscribe [::subs/response-code]) (:code status-code))
         :value    (:code status-code)}
        (str (:code status-code) ": " (:text status-code))])]])

(defn- status-code [code]
  [:div.flex.items-center
   [:p.rounded-full.h-4.w-4
    {:style
     {:background-color (code-color code)}}]
   [:p.ml-1
    [:span.mr-1 (str code)]
    [:span.font-medium (some #(when (= (:code %) code) (:text %)) http-status-codes)]]])

(defn- headers-field [headers]
  [:div.my-5
   [:p.text-2xl.mb-1 "Headers"]
   [:table.table-auto.pt-1
    [:thead
     [:tr
      [:th.text-left "Key"]
      [:th.text-left "Value"]]]
    [:tbody
     (for [header headers]
       ^{:key header}
       [:tr
        [:td.border.px-4.py-2 (key header)]
        [:td.border.px-4.py-2 (val header)]])]]])

(defn- active-header-field []
  [:div.mt-5
   [:div.flex.justify-between
    [:span.text-xl.mb-2 "Headers"]
    [:span.text-2xl.cursor-pointer.text-green-500
     {:on-click #(dispatch [::events/update-data [:response :headers] conj {}])}
     "+"]]
   (doall
    (map-indexed
      (fn [index item]
       ^{:key (str "active-header-field-" index)}
       [:div.flex.justify-between.mb-2
        [:input
         {:class (str input-class-text " mr-2 py-2 px-3 rounded")
          :on-change #(dispatch [::events/update-response-header index :key (-> % .-target .-value)])
          :value (:key item)}]
        [:input
         {:class (str input-class-text " py-2 px-3 rounded")
          :on-change #(dispatch [::events/update-response-header index :val (-> % .-target .-value)])
          :value (:val item)}]]) @(subscribe [::subs/response-header])))])

(defn- active-body-type []
  [:select.block.my-2
    {:on-change #(dispatch [::events/add-data [:response :type] (-> % .-target .-value)])
     :class     (str select-class-text " py-3 px-4 pr-8 rounded")}
    (for [mime-type mime-types]
      ^{:key (str "active-body-type-" (:type mime-type))}
      [:option
       {:selected (= @(subscribe [::subs/response-type]) (:type mime-type))
        :value    (:type mime-type)}
       (:text mime-type)])])

(defn- active-body-field []
  [:div.mt-5
   [:p.text-xl "Body"]
   [active-body-type]
   [:textarea
    {:class       (str input-class-text " w-full py-2 px-3 rounded")
     :value       @(subscribe [::subs/response-body])
     :on-change   #(dispatch [::events/add-data [:response :body] (-> % .-target .-value)])
     :placeholder "Body"}]])

(defn- body-field [id body mime-type]
  [:div
   [:p.flex.items-center.justify-between.mb-1
    [:span.text-2xl.mr-1 "Body"]
    [:span.text-indigo-500 (some #(when (= (:type %) mime-type) (:text %)) mime-types)]]
   [:div.border.rounded.p-3.bg-gray-200.whitespace-pre-wrap
    (if (= mime-type "JSON")
      [:pre.whitespace-pre-wrap
       {:key id
        :dangerouslySetInnerHTML {:__html (.toHtml prettyPrintJson (.parse js/JSON body))}}]
      body)]])

(defn- response-list-view []
  [:div.w-full.mt-2.pb-2
   [:div.flex.items-center.justify-between
    [:span.text-2xl "Responses"]
    [:span.text-3xl.cursor-pointer.text-green-500
     {:on-click #(dispatch [::events/open-modal-for-create])}
     [:i.fas.fa-plus-circle]]]
   (for [response @(subscribe [::subs/responses])]
     ^{:key (str "response-list-" response)}
     [:div.border.rounded.shadow.p-5.my-5
      [:div.flex.justify-between
       [status-code (:code response)]
       [dropdown
        {:options [{:title "Update"
                    :class "hover:text-green-500"
                    :on-click-fn #(dispatch [::events/edit-response response])}
                   {:title "Delete"
                    :class "hover:text-red-500"
                    :on-click-fn #(dispatch [::events/delete-response (:id response)])}]}]]
      [headers-field (:headers response)]
      [body-field (:id response) (:body response) (:mimeType response)]])])

(defn- request-box-view [request]
  [:div.flex.w-full
   [:select
    {:class     (str select-class-text " py-3 px-4 pr-8 rounded-l")
     :on-change #(dispatch [::events/add-data [:request :method] (-> % .-target .-value)])}
    (for [method http-methods]
      ^{:key method}
      [:option
       {:selected (= method (:method request))}
       method])]
   [:input
    {:type        "text"
     :value       (:path request)
     :on-change   #(dispatch [::events/add-data [:request :path] (-> % .-target .-value)])
     :class       (str input-class-text " w-full py-2 px-3 rounded-r")
     :placeholder "Path"}]
   [:button
    {:class    (str "p-1 bg-transparent rounded border-2 shadow bg-white border-green-500 text-green-500 mx-1")
     :on-click #(dispatch [::events/update-request])}
    "Update"]
   [:button
    {:class (str "p-1 bg-transparent rounded border-2 shadow bg-white border-red-500 text-red-500")
     :on-click #(dispatch [::events/delete-request])}
    "Delete"]])

(defn- active-button-box []
  (let [response-id @(subscribe [::subs/response-id])]
    [:div.flex.justify-end.mt-5
     [:button
      {:class (str button-class-text " text-red-500 border-red-500 mr-2 hover:border-red-500")
       :on-click #(dispatch [::events/add-data [:modal-visible?] false])}
      "Cancel"]
     [:button
      {:class (str button-class-text " text-green-500 border-green-500 hover:border-green-500")
       :on-click #(dispatch [(if response-id ::events/update-response ::events/create-response)])}
      (if response-id "Update" "Save")]]))

(defn- create-modal-view []
  (r/create-class
    {:component-will-unmount #(dispatch [::events/reset :response])
     :reagent-render (fn []
                       [:div
                        {:class modal-class-text}
                        [:div.w-full.max-w-lg.p-5.relative.mx-auto.my-auto.rounded-xl.shadow-lg.bg-white
                         [:div
                          [active-status-code]
                          [active-header-field]
                          [active-body-field]
                          [active-button-box]]]])}))

(defn- empty-box []
  [:div.h-screen.flex.justify-center.items-center
   [:img
    {:src "/img/box.svg"
     :width 60}]])

(defn- base-url []
  (let [text (helper/base-url)]
    [:div.border.rounded.shadow.p-2.mb-5.flex.justify-between.items-center
     text
     [:i.fas.fa-copy.cursor-pointer
      {:on-click #(util/copy-to-clipboard! text)}]]))

(defn- socket-message-box []
  (let [messages @(subscribe [::subs/socket-messages])]
    [:div.w-full.lg:overflow-y-auto.bg-gray-200.p-5.xl:ml-5.mc-main
     [:p.text-2xl.flex.justify-between
      [:span.underline "Realtime Request Logs"]
      [:span.live-icon]]
     (for [message messages]
       ^{:key (:createdAt message)}
       [:<>
        [:pre.whitespace-pre-wrap.py-5
         {:key (:createdAt message)
          :style {:font-size 11}
          :dangerouslySetInnerHTML {:__html (.toHtml prettyPrintJson (clj->js message))}}]
        [:hr]])]))

(defn- main-view []
  [:div.p-5.xl:h-screen
   (if-let [request @(subscribe [::subs/request])]
     [:div.xl:flex.xl:justify-between.lg:overflow-y-auto
       [:div.w-full.xl:overflow-y-auto.mc-main
         [base-url]
         [request-box-view request]
         [response-list-view]]
      [socket-message-box]]
     [empty-box])])

(defn dashboard-view []
  (r/create-class
    {:component-did-mount #(dispatch [::events/get-workspaces])
     :component-will-unmount #(dispatch [::events/remove-dashboard-data])
     :reagent-render (fn []
                       [:div.relative.min-h-screen.lg:flex
                        [:div.xl:w-72.md:w-full.shadow-md.p-5.lg:shadow-none
                         [workspace-list-view]]
                        [:div.xl:w-72.md:w-full.shadow-md.p-5
                         [request-list-view]]
                        [:div.xl:flex-1.md:w-full
                         [main-view]]
                        (when @(subscribe [::subs/modal-visible?])
                          [create-modal-view])
                        (when-let [modal-type @(subscribe [::subs/modal-form-type])]
                         [create-modal-form modal-type])])}))
