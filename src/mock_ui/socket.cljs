(ns mock-ui.socket
  (:require [re-frame.core :refer [dispatch]]
            [mock-ui.events :as events]
            [mock-ui.util :as util]))

(defn- create-ws [url]
  (js/WebSocket. url))

(defn close-ws [socket]
  (.close socket))

(defn- handle-ws-msg [msg]
  (dispatch [::events/update-data [:socket-messages] conj
             (merge (js->clj (.parse js/JSON (.-data msg)) :keywordize-keys true)
                    {:createdAt (-> (util/moment) (util/format-date "DD/MM/yyyy HH:mm"))})]))

(defn connect-ws [url]
  (let [socket (create-ws url)]
    (dispatch [::events/add-data [:socket] socket])
    (set! (.-onmessage socket) handle-ws-msg)))

(defn disconnect-ws [socket]
  (close-ws socket)
  (dispatch [::events/reset :socket])
  (dispatch [::events/reset :socket-messages]))
