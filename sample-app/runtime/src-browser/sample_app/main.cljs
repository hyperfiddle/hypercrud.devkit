(ns sample-app.main
  (:require [hypercrud.browser.routing :as routing]
            [hypercrud.client.core :as hc]
            [hypercrud.client.internal :refer [transit-decode]]
            [hypercrud.client.peer :as peer]
            [hypercrud.state.actions.core :as actions]
            [hypercrud.state.core :as state]
            [hypercrud.state.reducers :as reducers]
            [hypercrud.ui.navigate-cmp :as navigate-cmp]
            [pushy.core :as pushy]
            [reagent.core :as reagent]
            [sample-app.core :as app]))


(enable-console-print!)

(def service-uri (goog.Uri. "http://localhost:8080/api/"))

(defonce params (let [s-params (-> (js/document.getElementById "params") .-innerHTML)]
                  (if-not (empty? s-params)
                    (transit-decode s-params))))

(set! hc/*root-conn-id* 17592186045435 #_(:root-conn-id params))

(defonce state-atom
  (reagent/atom (-> (merge {:entry-uri service-uri} {} #_(:??? params))
                    (reducers/root-reducer nil))))

(def dispatch! (state/build-dispatch state-atom reducers/root-reducer))

(defonce history (pushy/pushy (fn [page-path]
                                (dispatch! (actions/set-route-encoded page-path app/index-link)))
                              identity))

(defonce param-ctx
  (let [peer (peer/->Peer state-atom)]
    {:dispatch dispatch!
     :peer peer
     :root-db (hc/db peer hc/*root-conn-id* nil)
     :display-mode :user
     :navigate-cmp navigate-cmp/navigate-cmp}))

(set! state/*request* #(app/request % param-ctx))

(def ui
  (reagent/create-class
    {:reagent-render (fn [] [app/view state-atom param-ctx])
     :component-did-mount
     (fn [this]
       (add-watch state-atom :browser-sync!
                  (fn [k r o n]
                    (when (not= (:route o) (:route n))
                      (pushy/set-token! history (routing/encode (:route n)))))))
     :component-will-unmount
     (fn [this]
       (remove-watch state-atom :browser-sync!))}))

(defn mount-ui []
  (reagent/render [ui] (.getElementById js/document "root")))

(defn -main []
  (pushy/start! history)
  (mount-ui))
