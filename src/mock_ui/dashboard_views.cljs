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
                       z-50 outline-none focus:outline-none bg-gray-500 bg-opacity-50")

(def button-class-text "text-xl border-2 rounded p-1 bg-transparent")

(defn- create-workspace-form []
  [:div.flex.m-5
   [:input
    {:class       (str input-class-text " p-2 rounded-l")
     :placeholder "Workspace"
     :on-change   #(dispatch [::events/add-data [:create-form :workspace :name] (-> % .-target .-value)])}]
   [:button
    {:class    "p-1 bg-transparent rounded-r border shadow bg-yellow-300 border-yellow-300"
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
    {:class    "p-1 bg-transparent rounded-r border shadow bg-yellow-300 border-yellow-300"
     :on-click #(dispatch [::events/create-request])}
    "Create"]])

(defn- create-modal-form []
  (r/create-class
    {:component-will-unmount #(dispatch [::events/reset :create-form])
     :reagent-render (fn [modal-type]
                        [:div
                         {:class modal-class-text}
                         [:div.w-full.max-w-lg.p-5.relative.mx-auto.my-auto.rounded-xl.shadow-lg.bg-white
                          [:img.absolute.top-2.right-2.text-red-500.cursor-pointer
                           {:src      "/img/remove-button.svg"
                            :on-click #(dispatch [::events/reset :create-form])
                            :width    20}]
                          (if (= :workspace modal-type)
                            [create-workspace-form]
                            [create-request-form])]])}))

(defn- dropdown [options dropdown-class]
  [:div.dropdown.inline-block
   {:class dropdown-class}
   [:button.text-gray-700.font-semibold.rounded.inline-flex.items-center
    [:img
     {:src "/img/menu.svg"
      :width 20}]]
   [:ul.dropdown-menu.absolute.hidden.text-gray-700.z-40.border.rounded.bg-white
    (for [option options]
      [:li.bg-transparent
       {:class (:class option)}
       [:a.py-2.px-4.block.whitespace-no-wrap.cursor-pointer
        {:on-click (:on-click-fn option)}
        (:title option)]])]])

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
      [dropdown [{:title "Delete"
                  :class "hover:text-red-500"
                  :on-click-fn #(dispatch [::events/delete-workspace (:id workspace)])}]]])
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
      {:on-click #(dispatch [::events/get-responses (:id request)])}
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
     {:class (str select-class-text " py-3 px-4 pr-8 rounded")
      :on-change #(dispatch [::events/add-data [:response :code] (-> % .-target .-value js/parseInt)])}
     (for [status-code http-status-codes]
       ^{:key (str "active-status-code-" (:code status-code))}
       [:option
        {:selected (= @(subscribe [::subs/response-code]) (:code status-code))
         :value (:code status-code)}
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
     :class (str select-class-text " py-3 px-4 pr-8 rounded")}
    (for [mime-type mime-types]
      ^{:key (str "active-body-type-" (:type mime-type))}
      [:option
       {:selected (= @(subscribe [::subs/response-type]) (:type mime-type))
        :value (:type mime-type)}
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

(defn- body-field [body mime-type]
  [:div
   [:p.flex.items-center.justify-between.mb-1
    [:span.text-2xl.mr-1 "Body"]
    [:span.text-indigo-500 (some #(when (= (:type %) mime-type) (:text %)) mime-types)]]
   [:div.border.rounded.p-3.bg-gray-200.whitespace-pre-wrap body]])

(defn- response-list-view []
  [:div.w-full.mt-2
   [:div.flex.items-center.justify-between
    [:span.text-2xl "Responses"]
    [:span.text-3xl.cursor-pointer.text-indigo-500
     {:on-click #(dispatch [::events/open-modal-for-create])}
     "+"]]
   (for [response @(subscribe [::subs/responses])]
    [:div.border.rounded.shadow.p-5.my-5.relative
     [dropdown [{:title "Update"
                 :class "hover:text-yellow-500"
                 :on-click-fn #(dispatch [::events/edit-response response])}
                {:title "Delete"
                 :class "hover:text-red-500"
                 :on-click-fn #(dispatch [::events/delete-response (:id response)])}]
               "absolute top-5 right-10"]
     [status-code (:code response)]
     [headers-field (:headers response)]
     [body-field (:body response) (:mimeType response)]])])

(defn- request-box-view []
  (let [request @(subscribe [::subs/selected-request])]
    [:div
      [:p.text-2xl "Request"]
      [:div.flex.w-full
       [:select
        {:class (str select-class-text " py-3 px-4 pr-8 rounded-l")}
        (for [method http-methods]
          ^{:key method}
          [:option
           {:selected (= method (:method request))}
           method])]
       [:input
        {:type        "text"
         :value       (:path request)
         :class       (str input-class-text " w-full py-2 px-3")
         :placeholder "Path"}]
       [:button
        {:class (str "p-1 bg-transparent border shadow bg-yellow-300 border-yellow-300")}
        "Update"]
       [:button
        {:class (str "p-1 bg-transparent rounded-r border shadow bg-red-300 border-red-300")}
        "Delete"]]]))

(defn- active-button-box []
  (let [response-id @(subscribe [::subs/response-id])]
    [:div.flex.justify-end.mt-5
     [:button
      {:class (str button-class-text " text-red-500 border-red-300 mr-2 hover:border-red-500")
       :on-click #(dispatch [::events/add-data [:modal-visible?] false])}
      "Cancel"]
     [:button
      {:class (str button-class-text " text-green-500 border-green-300 hover:border-green-500")
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

(defn- main-view []
  [:div.px-10.py-5
   [request-box-view]
   [response-list-view]
   (when @(subscribe [::subs/modal-visible?])
    [create-modal-view])
   (when-let [modal-type @(subscribe [::subs/modal-form-type])]
     [create-modal-form modal-type])])

(defn dashboard-view []
  (r/create-class
    {:component-did-mount #(dispatch [::events/get-workspaces])
     :reagent-render (fn []
                       [:div.relative.min-h-screen.flex
                        [:div.w-72.p-5
                         [workspace-list-view]]
                        [:div.w-72.shadow-md.p-5
                         [request-list-view]]
                        [:div.flex-1.h-screen.overflow-y-auto
                         [main-view]]])}))
