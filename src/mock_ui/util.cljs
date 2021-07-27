(ns mock-ui.util)

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
    (as-> (.getItem js/localStorage "user") data
      (.parse js/JSON data)
      (js->clj data :keywordize-keys true))
    (catch js/Error _
      {})))

(defn sleep
  [f ms]
  (js/setTimeout f ms))

(defn find-kv
  "coll is vector
   k is key
   v is value"
  [coll k v]
  (some #(when (= v (k %)) %) coll))

(defn find-and-all-update
  [k d v coll]
  (mapv #(if (= (k %) d) v %) coll))
