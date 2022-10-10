(ns reduce-fsm.fsm
  (:require-macros reduce-fsm.fsm)
  (:require
    cljs.core.match
    reduce-fsm.fsm.impl))

(defn fsm-event
  "process a single event with an incremental finite state machine (those created with fsm-inc or defsm-inc)
Returns a map with the following keys:
  :state          - the current state of the fsm after processing the event
  :value          - the current accumulator value
  :is-terminated? - true when the fsm is in a terminal state and no more events can be processed"
  [fsm event]
  {:pre [(map? fsm) (contains? fsm :fsm)]} ; only valid for incremental fsms
  (if (:is-terminated? fsm)
    fsm
    ((:fsm fsm) (:value fsm) event)))
