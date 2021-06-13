(ns mock-ui.db)


(def response-headers [{:key "name" :val "burkay"}
                       {:key "surname" :val "Durdu"}
                       {:key "age" :val "34"}])

(def default-db
  {:name "Mock API"
   :modal-visible? false
   :response {:headers response-headers}})
