(ns mock-ui.dashboard-views
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [mock-ui.subs :as subs]
            [mock-ui.events :as events]))

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

(defn- code-color [code]
  (cond
    (and (>= code 100) (< code 200)) "#a0aec0"
    (and (>= code 200) (< code 300)) "#48bb78"
    (and (>= code 300) (< code 400)) "#ecc94b"
    (and (>= code 400) (< code 500)) "#ed8936"
    (and (>= code 500) (< code 600)) "#f56565"
    :else "#4299e1"))

(def input-class-text "flex-grow shadow appearance-none border rounded
                       py-2 px-3 text-gray-700 leading-tight
                       focus:outline-none focus:shadow-outline")

(def select-class-text "flex-grow-0 shadow appearance-none bg-gray-200
                        border border-gray-200 text-gray-700 py-3 px-4
                        pr-8 rounded leading-tight focus:outline-none
                        focus:bg-white focus:border-gray-500 cursor-pointer")

(def modal-class-text "min-w-screen h-screen animated fadeIn faster fixed
                       left-0 top-0 flex justify-center items-center inset-0
                       z-50 outline-none focus:outline-none bg-gray-500 bg-opacity-50")

(def button-class-text "text-xl border-2 rounded p-1 bg-transparent")

(defn- workspace-list-view []
  [:div
   [:p.text-2xl "Workspaces"]
   (for [workspace @(subscribe [::subs/workspaces])]
     ^{:key (:id workspace)}
     [:div (:name workspace)])])

(defn- request-list-view []
  [:div
   [:p.text-2xl "Requests"]
   (for [request @(subscribe [::subs/requests])]
     ^{:key (:id request)}
     [:div
      [:span (str "[" (:method request) "]")]
      [:span (:path request)]])])

(defn- active-status-code []
  [:div.mb-2
   [:p.text-xl.mb-1 "Status Code"]
   [:select.block
     {:class select-class-text
      :on-change #(dispatch [::events/add-data [:response :code] (-> % .-target .-value js/parseInt)])}
     (for [status-code http-status-codes]
       ^{:key (str "active-status-code-" (:code status-code))}
       [:option
        {:value (:code status-code)}
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
         {:class (str input-class-text " mr-2")
          :on-change #(dispatch [::events/update-response-header index :key (-> % .-target .-value)])
          :value (:key item)}]
        [:input
         {:class input-class-text
          :on-change #(dispatch [::events/update-response-header index :val (-> % .-target .-value)])
          :value (:val item)}]]) @(subscribe [::subs/response-header])))])

(defn- active-body-type []
  [:select.block.my-2
    {:on-change #(dispatch [::events/add-data [:response :type] (-> % .-target .-value)])
     :class select-class-text}
    (for [mime-type mime-types]
      ^{:key (str "active-body-type-" (:type mime-type))}
      [:option
       {:value (:type mime-type)}
       (:text mime-type)])])

(defn- active-body-field []
  [:div.mt-5
   [:p.text-xl "Body"]
   [active-body-type]
   [:textarea
    {:class       (str input-class-text " w-full")
     :value       @(subscribe [::subs/response-body])
     :on-change   #(dispatch [::events/add-data [:response :body] (-> % .-target .-value)])
     :placeholder "Body"}]])

(defn- body-field [body mime-type]
  [:div
   [:p.flex.items-center.justify-between.mb-1
    [:span.text-2xl.mr-1 "Body"]
    [:span.text-indigo-500 (some #(when (= (:type %) mime-type) (:text %)) mime-types)]]
   [:div.border.rounded.p-3.bg-gray-200.whitespace-pre-wrap body]])

(defn- action-button-select []
  [:select.absolute.top-5.right-5
   [:option
    {:on-click #()}
    "Edit"]
   [:option
    {:on-click #()}
    "Delete"]])

(defn- response-list-view []
  [:div.w-full.mt-2
   [:div.flex.items-center.justify-between
    [:span.text-2xl "Responses"]
    [:span.text-3xl.cursor-pointer.text-indigo-500
     {:on-click #(dispatch [::events/open-modal-for-create])}
     "+"]]
   (for [response @(subscribe [::subs/responses])]
    [:div.border.rounded.shadow.p-5.my-5.relative
     [action-button-select]
     [status-code (:code response)]
     [headers-field (:headers response)]
     [body-field (:body response) (:mimeType response)]])])

(defn- request-box-view []
  [:div
    [:p.text-2xl "Request"]
    [:div.flex.w-full
     [:select
      {:class select-class-text}
      (for [method http-methods]
        ^{:key method}
        [:option method])]
     [:input
      {:type        "text"
       :class       (str input-class-text " w-full")
       :placeholder "Path"}]]])

(defn- active-button-box []
  [:div.flex.justify-end.mt-5
   [:button
    {:class (str button-class-text " text-red-500 border-red-300 mr-2 hover:border-red-500")
     :on-click #(dispatch [::events/add-data [:modal-visible?] false])}
    "Cancel"]
   [:button
    {:class (str button-class-text " text-green-500 border-green-300 hover:border-green-500")
     :on-click #(dispatch [::events/save])}
    "Save"]])

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

(defn- main-view []
  [:div.p-10
   [request-box-view]
   [response-list-view]
   (when @(subscribe [::subs/modal-visible?])
    [create-modal-view])])

(defn dashboard-view []
  (r/create-class
    {:reagent-render (fn []
                       [:div.relative.min-h-screen.flex
                        [:div.w-64.p-10
                         [workspace-list-view]]
                        [:div.w-64.shadow-md.p-10
                         [request-list-view]]
                        [:div.flex-1.h-screen.overflow-y-auto
                         [main-view]]])}))
