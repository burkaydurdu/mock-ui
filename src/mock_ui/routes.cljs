(ns mock-ui.routes
  (:require
   [bidi.bidi :as bidi]
   [pushy.core :as pushy]
   [cemerick.url :as url]
   [re-frame.core :as re-frame]
   [mock-ui.events :as events]))

(defmulti panels identity)
(defmethod panels :default [] [:div "No panel found for this route."])

(def routes
  (atom
    ["/" {""          :home
          "sign-up"   :sign-up
          "sign-in"   :sign-in
          "dashboard" :dashboard}]))

(defn parse
  [url]
  (bidi/match-route @routes url))

(defn url-for
  [& args]
  (apply bidi/path-for (into [@routes] args)))

(defn dispatch
  [route]
  (let [panel (keyword (str (name (:handler route)) "-panel"))]
    (re-frame/dispatch [::events/set-active-panel panel])))

(defonce history
  (pushy/pushy dispatch parse))

(defn window-origin []
  (if-let [origin (-> js/window .-location .-origin)]
    origin
    (str (-> js/window .-location .-protocol) "//"
         (-> js/window .-location .-hostname)
         (if-let [port (-> js/window .-location .-port)]
           (str ":" port)
           ""))))

(defn set-uri-token! [uri]
  (let [u (url/url (window-origin))
        k (str u uri)]
    (pushy/set-token! history k)
    (dispatch (parse uri))))

(defn navigate!
  [handler]
  (pushy/set-token! history (url-for handler)))

(defn start!
  []
  (pushy/start! history))
