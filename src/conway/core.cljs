(ns ^:figwheel-hooks conway.core
  (:require
   [goog.dom :as gdom]
   [reagent.core :as reagent :refer [atom]]))

(defn neighbors [[x y]]
  (for [dx [-1 0 1] dy [-1 0 1]
        :when (not (= 0 dx dy))]
    [(+ x dx) (+ y dy)]))

(defn step [lives]
  (set (for [[pos live-neighbors]
             (frequencies (mapcat neighbors lives))
             :when (or (= 3 live-neighbors)
                       (and (contains? lives pos)
                            (= 2 live-neighbors)))]
         pos)))

(def status (atom "not started"))

(def world-size (atom 10))

(def lives (atom #{}))

(def timer-id (atom 0))

(def timer (atom :off))

(defn start-timer! []
  (when (= @timer :off)
    (reset! timer-id (js/setInterval #(swap! lives step) 300))
    (reset! timer :on)))

(defn stop-timer! [timer]
  (js/clearInterval timer))

(defn size-input []
  (fn []
    [:div
     [:p "World size: "
      [:input {:type "number"
               :value @world-size
               :on-change #(reset! world-size (-> % .-target .-value))}]]]))

(defn wrap-square [[x y]]
  [(mod x (js/parseInt @world-size)) (mod y (js/parseInt @world-size))])

(defn wrap-squares [lives]
  (map wrap-square lives))

(def glider
  {:name "Glider"
   :lives #{[3 4] [4 4] [5 4] [5 3] [4 2]}
   :size 10})

(def spaceship
  {:name "Spaceship"
   :lives #{[4 3] [5 3] [7 2] [5 5] [8 3] [7 4] [5 4]
            [6 5] [4 4] [7 3] [6 2] [6 4]}
   :size 10})

(def penta-decathlon
  {:name "Penta-decathlon"
   :lives
   #{[8 5] [5 5] [13 5] [11 4] [4 5] [12 5]
     [9 5] [6 6] [7 5] [10 5] [11 6] [6 4]}
  :size 18})

(def glider-gun
  {:name "Glider gun"
   :lives #{[27 18] [32 13] [25 13] [28 15] [36 16]
            [25 19] [28 17] [47 13] [28 16] [33 14]
            [34 12] [36 12] [27 14] [24 19] [26 16]
            [23 18] [34 16] [32 15] [12 15] [33 13]
            [36 17] [46 14] [29 16] [36 11] [12 16]
            [22 15] [23 14] [24 13] [22 16] [22 17]
            [32 14] [33 15] [13 16] [47 14] [13 15] [46 13]}
   :size 30})

(defn rect-cell [x y]
  [:rect.cell
   {:x x :width 1
    :y y :height 1
    :fill "white"
    :stroke-width 0.025
    :stroke "black"}])

(defn dead [x y]
  [:rect
   {:width 0.9
    :height 0.9
    :fill "lightpink"
    :x (+ 0.05 x)
    :y (+ 0.05 y)
    :on-click
    (fn click-square [e]
      (if (= @status "not started")
        (swap! lives conj [x y])
        (js/alert "Hands off! You are not God.")))}])

(defn life [x y]
  [:rect
   {:width 0.9
    :height 0.9
    :fill "purple"
    :x (+ 0.05 x)
    :y (+ 0.05 y)
    :on-click
    (fn click-square [e]
      (if (= @status "not started")
        (swap! lives disj [x y])
        (js/alert "Ouch!")))}])

(defn render-board []
  (into
   [:svg.board
    {:view-box (str "0 0 " @world-size " " @world-size)
     :shape-rendering "auto"
     :style {:max-height "500px"}}]
   (for [x (range @world-size)
         y (range @world-size)]
     [:g
      [rect-cell x y]
      (if (some #{[x y]} (wrap-squares @lives))
        [life x y]
        [dead x y])])))

(defn config-button [m]
  [:button
   {:on-click
    (fn step-click [e]
      (reset! lives (:lives m))
      (reset! world-size (:size m)))}
   (:name m)])

(defn game []
  [:center
   [:h1 "Conway's Game of Life"]
   [size-input]
   [:div
    [:button
     {:on-click
      (fn step-click [e]
        (swap! lives step)
        (reset! status "started"))}
     "Step"]
    [:button
     {:on-click
      (fn play-click [e]
        (start-timer!)
        (reset! status "started"))}
     "Play"]
    [:button
     {:on-click
      (fn stop-click [e]
        (stop-timer! @timer-id)
        (reset! timer :off))}
     "Stop"]
    [:button
     {:on-click
      (fn reset-click [e]
        (reset! lives #{})
        (reset! status "not started"))}
     "Reset"]]
   [:div
    [config-button glider]
    [config-button spaceship]
    [config-button penta-decathlon]
    [config-button glider-gun]]
   [:div [render-board]]
   [:p (str "Current residents: " @lives)]
   [:p (str "Wrapped to: " (wrap-squares @lives))]])

(defn get-app-element []
  (gdom/getElement "app"))

(defn mount [el]
  (reagent/render-component [game] el))

(defn mount-app-element []
  (when-let [el (get-app-element)]
    (mount el)))

;; conditionally start your application based on the presence of an "app" element
;; this is particularly helpful for testing this ns without launching the app
(mount-app-element)

;; specify reload hook with ^;after-load metadata
(defn ^:after-load on-reload []
  (mount-app-element)
  ;; optionally touch your a

;; define your app data so that it doesn't get over-written on reloadpp-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )
