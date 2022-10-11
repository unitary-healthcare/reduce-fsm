(ns reduce-fsm.visualize
  "Visualize finite state machines"
  (:require
    [clojure.core.match :refer [match]]
    [clojure.core.match.regex]
    [clojure.set :as set]
    [dorothy.core :as d]
    [dorothy.jvm :as d.jvm]))

(defn- dot-exists
  "return true if the dot executable from graphviz is available on the path"
  [& _]
  (try
    (->> "dot -V"
      (.exec (Runtime/getRuntime))
      (.waitFor)
      (= 0))
    (catch Exception _ false)))

(defn- no-graphviz-message
  []
  (println "The dot executable from graphviz was not found on the path, unable to draw fsm diagrams")
  (println "Download a copy from http://www.graphviz.org/"))

(defn- graphviz-installed?
  []
  (if (dot-exists)
    true
    (do
      (no-graphviz-message)
      false)))

(defn- dorothy-state
  "Create a single dorothy state"
  [fsm-type {:keys [params name state]}]
  (let [is-terminal? (if (= :fsm-filter fsm-type)
                       (not (:pass params true))
                       (or (:is-terminal params)
                         (= \( (first name))))]
    [state
     (merge {:label name}
       (when is-terminal?
         {:style "filled,setlinewidth(2)"
          :fillcolor "grey88"}))]))

(defn- transitions-for-state
  "return a sequence of dorothy transitions for a single state"
  [state]
  (letfn [(transition-label
            [trans idx]
            (str
              (format "<TABLE BORDER=\"0\"><TR><TD TITLE=\"priority = %d\">%s</TD></TR>" idx (:evt trans))
              (when (:action trans)
                (format "<TR><TD>(%s)</TD></TR>" (:action trans)))
              (when (:emit trans)
                (format "<TR><TD>(%s) -&gt;</TD></TR>" (:emit trans)))
              "</TABLE>"))
          (format-trans
            [trans idx]
            [(:from-state trans) (:to-state trans) {:label (transition-label trans idx)}])]
    (map format-trans (:transitions state) (range (count (:transitions state))))))

(defn fsm-dorothy
  "Create a dorothy digraph definition for an fsm"
  [fsm]
  (let [start-state (keyword (gensym "start-state"))
        state-map (->> fsm meta :reduce-fsm/states)
        fsm-type (->> fsm meta :reduce-fsm/fsm-type)]
    (d/digraph
      (concat
        [[start-state {:label "start" :style :filled :color :black :shape "point" :width "0.2" :height "0.2"}]]
        (map (partial dorothy-state fsm-type) state-map)
        [[start-state (-> state-map first :state)]]
        (mapcat transitions-for-state state-map)))))

(defn fsm-dot
  "Create the graphviz dot output for an fsm"
  [fsm]
  (d/dot (fsm-dorothy fsm)))

(defn- show-dorothy-fsm
  [fsm]
  (d.jvm/show! (fsm-dot fsm)))

(defn show-fsm
  "Display the fsm as a diagram using graphviz (see http://www.graphviz.org/)"
  [fsm]
  (when (graphviz-installed?)
    (show-dorothy-fsm fsm)))

(defn save-fsm-image
  "Save the state transition diagram for an fsm as a png.
Expects the following parameters:
  - fsm      - the fsm to render
  - filename - the output file for the png."
  [fsm filename]
  (when (graphviz-installed?)
    (d.jvm/save! (fsm-dot fsm) filename {:format :png}))
  nil)
