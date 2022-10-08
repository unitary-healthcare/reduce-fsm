# reduce-fsm

This is a fork of the [cdorrat/reduce-fsm][gh:cdorrat] repository.

[gh:cdorrat]: https://github.com/cdorrat/reduce-fsm

## Features

reduce-fsm provides a simple way to specify clojure [finite state
machines](http://en.wikipedia.org/wiki/Finite-state_machine), it allows you
to:

- Define define state machines that accumulate values (in the same was that
  reduce does)
- Create lazy sequences from state machines
- Perform stateful filtering with clojures filter/remove functions
- Visualize the resulting state machines with graphviz

All generated state machines are plain clojure functions and read events from
clojure sequences.  Events are dispatched with core.match and allow the use of
all match features (guards, destructuring, regex matching, etc.)

## Usage

Use via:

```clojure
(require '[reduce-fsm :as fsm])
```

## Examples

### Basic FSM

The following example counts the number of times "ab" occurs in a sequence.

```clojure
(defn inc-val [val & _] (inc val))

(fsm/defsm count-ab
  [[:start
    \a -> :found-a]
   [:found-a
    \a ->  :found-a
    \b -> {:action inc-val} :start
    _ -> :start]])

;; We can use the generated fsm like any function
(map (partial count-ab 0) ["abaaabc" "aaacb" "bbbcab"])
;; returns => (2 0 1)

(fsm/show-fsm count-ab)
;; displays the fsm diagram below

```

> :warning: **TODO:** Fix broken image.

![show-fsm output](http://cdorrat.github.com/reduce-fsm/images/fsm-count-ab.png)

### Incremental FSM

The following example repeats the state machine from the Basic FSM example but
uses function calls to provide events instead of clojure sequences. This can
be useful when you have multiple event sources or events are generated by
callbacks.

```clojure
(defn inc-val [val & _] (inc val))

(fsm/defsm-inc count-ab
  [[:start
    \a -> :found-a]
   [:found-a
    \a ->  :found-a
    \b -> {:action inc-val} :start
    _ -> :start]])

;; create an instance of the fsm with an initial value of 0
(def fsm-state (atom (count-ab 0)))

;; update the state with a few events
(swap! fsm-state fsm/fsm-event \a)
(swap! fsm-state fsm/fsm-event \a)
(swap! fsm-state fsm/fsm-event \b)

(:value @fsm-state)
;; returns the current accumulated value => 1

(:state @fsm-state)
;; the current state of the fsm => :start

;; count the number of ab occurences in a string
(:value (reduce fsm/fsm-event (count-ab 0) "abaaabc"))
;; => 2

```

### Generating Lazy Sequences

The fsm-seq functions return lazy sequences of values created by the emit
function when a state change occurs.  This example looks for log lines where
the sequence of events was (a,c) instead of the expected (a,b,c) and adds the
unexpected event to the output sequence.


```clojure
(defn emit-evt [val evt] evt)

(defsm-seq log-search
  [[:start
    #".*event a" -> :found-a]
   [:found-a
    #".*event b" -> :found-b
    #".*event c" -> {:emit emit-evt} :start]
   [:found-b
    #".*event c" -> :start]])

;; The resulting function accepts a sequence of events
;; and returns a lazy sequence of emitted values
(take 2 (log-search (cycle ["1 event a"
                            "2 event b"
                            "3 event c"
                            "another event"
                            "4 event a"
                            "event x"
                            "5 event c"])))

;; returns => ("5 event c" "5 event c")

(fsm/show-fsm log-search)
;; displays the image below

```

> :warning: **TODO:** Fix broken image.

![show-fsm output](http://cdorrat.github.com/reduce-fsm/images/fsm-log-search.png)

### Stateful Filtering

States in filters are defined as passing values (default) or suppressing them
{:pass false}.  For each event the filter will return the pass value of the
state it is in after processing the event (input sequence element).

The following example suppresses values from the time a 3 is encountered until
we see a 6.

```clojure
(defsm-filter sample-filter
  [[:initial
    3 -> :suppressing]
   [:suppressing {:pass false}
    6 -> :initial]])

;; The resulting fsm is used with the clojure.core/filter and remove
;; functions like this.
(filter (sample-filter) [1 2 3 4 5 1 2 6 1 2])
;; returns => (1 2 6 1 2)

(fsm/show-fsm sample-filter)
;; displays the diagram below
```

![show-fsm output](http://cdorrat.github.com/reduce-fsm/images/fsm-sample-filter.png)

### Different dispatch types

When defining a state machine the matching rules for a transition only use the
current event by default, by adding the :dispatch option you can make
transitions conditional on the state as well as the current event.  The
following dispatch types are supported:

- `:event-only` (default): just the current event is available for matches
  (equivalent to `(clojure.core.match/match evt ...)`)
- `:event-and-acc`: both the current accumulated state and the event are
  passed (equivalent to `(clojure.core.match/match [state evt] ...)`)
- `:event-acc-vec`: the state and event are passed in a single vector
  (equivalent to `(clojure.core.match/match [ [state evt] ] ...)`)

The following example demonstrates `:event-acc-vec` dispatch.

```clojure
(defn should-transition? [[state event]]
  (= (* state 2) event))

(defn event-is-even? [[state event]]
  (even? event))

(defn inc-count [cnt & _ ]
  (inc cnt))

(defn reset-count [& _]
  0)

;; transition to the next state when we get a value thats twice the number
;; of even events we've seen
(fsm/defsm even-example
	   [[:start
	     [_ :guard should-transition?] -> {:action reset-count} :next-state
	     [_ :guard event-is-even?] -> {:action inc-count} :start]
	    [:next-state ,,,]]
	   :default-acc  0
	   :dispatch :event-acc-vec)

(even-example [1 1 2])   ;; => 1 (the number of even events)
(even-example [1 2 2 4]) ;; => 0 (we transitioned to next state)
```

### Other examples

There are additional exmaples on [github](https://github.com/cdorrat/reduce-fsm/tree/master)
in the examples and test directories  including:

- a simple tcp server
- matching repeating groups
- using the :event-and-acc match syntax
- using guards on events

## License

Copyright (C) 2011 Cameron Dorrat

Distributed under the Eclipse Public License, the same as Clojure.
