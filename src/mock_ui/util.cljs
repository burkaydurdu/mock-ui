(ns mock-ui.util
  (:require [ajax.core :as ajax]
            [mock-ui.subs :as subs]
            [re-frame.core :refer [subscribe]]))

(goog-define api-url "http://localhost:8080")

(defn create-request-map
  ([type uri]
   (create-request-map type uri nil nil))
  ([type uri on-success]
   (create-request-map type uri on-success nil))
  ([type uri on-success on-fail]
   (cond-> {:headers         {"Authorization" @(subscribe [::subs/token])}
            :method          type
            :uri             (str api-url uri)
            :format          (ajax/json-request-format)
            :response-format (ajax/json-response-format {:keywords? true})
            :on-success      (if (vector? on-success) on-success [on-success])
            :on-failure      (if (vector? on-fail) on-fail [on-fail])}
     (nil? on-success) (assoc :on-success [:no-http-on-ok])
     (nil? on-fail) (assoc :on-failure [:no-http-on-failure]))))

(defn dissoc-in
  ([m ks]
   (if-let [[k & ks] (seq ks)]
     (if (seq ks)
       (let [v (dissoc-in (get m k) ks)]
         (if (empty? v)
           (dissoc m k)
           (assoc m k v)))
       (dissoc m k))
     m))
  ([m ks & kss]
   (if-let [[ks' & kss] (seq kss)]
     (recur (dissoc-in m ks) ks' kss)
     (dissoc-in m ks))))

(defn contains-many? [m & ks]
  (every? #(contains? m %) ks))

(defn remove-items!
  [keys]
  (try
    (doseq [k keys]
      (.removeItem (.-localStorage js/window) k))
    (catch js/Error e
      (println e))))

(defn remove-item!
  [key]
  (remove-items! [key]))

(defn set-item!
  [key val]
  (try
    (.setItem (.-localStorage js/window) key (.stringify js/JSON (clj->js val)))
    (catch js/Error e
      (println e)
      (remove-item! key))))

(defn get-current-user-map []
  (try
    (into (sorted-map)
          (as-> (.getItem js/localStorage "user") data
            (.parse js/JSON data)
            (js->clj data :keywordize-keys true)))
    (catch js/Error _
      {})))

(defn sleep
  [f ms]
  (js/setTimeout f ms))
