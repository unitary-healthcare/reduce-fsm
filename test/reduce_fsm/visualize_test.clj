(ns reduce-fsm.visualize-test
  (:require
    [clojure.test :refer [deftest is]]
    [reduce-fsm.fsm :refer [defsm]]
    [reduce-fsm.visualize])
  (:import java.awt.Frame))

(defn- test-save-line
  [state evt _from-state _to-state]
  (conj state evt))

(defsm log-search-fsm-test
  [[:waiting-for-a
    #".*event a" -> :waiting-for-b]
   [:waiting-for-b
    #".*event b" -> :waiting-for-c
    #".*event c" -> {:action test-save-line} :waiting-for-a]
   [:waiting-for-c
    #".*event c" -> :waiting-for-a]])

(deftest display-dorothy-fsm-test
  (let [frame (#'reduce-fsm.visualize/show-dorothy-fsm log-search-fsm-test)]
    (is (some? frame))
    (when frame
      (.dispose ^Frame frame))))
