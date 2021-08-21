(ns mock-ui.helper
  (:require [ajax.core :as ajax]
            [mock-ui.subs :as subs]
            [mock-ui.util :as util]
            [re-frame.core :refer [subscribe]]))

(goog-define api-url "http://localhost:8080")

(goog-define socket-url "ws://localhost:8080")

(defn socket-connection-url [user-id workspace-id request-id]
  (util/format "%s/request/%s/%s/%s" socket-url user-id workspace-id request-id))

(defn base-url []
  (str api-url
       "/api/"
       (:id @(subscribe [::subs/current-user]))
       "/"
       (:name @(subscribe [::subs/selected-workspace]))
       "/"))

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
