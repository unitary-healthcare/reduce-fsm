(ns reduce-fsm.fsm.impl)

(defn lookup-state
  [state-fn-map the-state]
  (if-let [a-state-fn (get state-fn-map the-state)]
    a-state-fn
    (throw (new #?(:clj RuntimeException
                   :cljs js/Error)
             (str "Could not find the state \"" the-state "\"")))))

(defn state-fn-name
  "Create a name for the internal function that will represent this state.
   We want it to be recognisable to the user so stack traces are more intelligible"
  [sym]
  (cond
    (fn? sym) (let [fn-name (-> sym meta :name str)]
                (if (empty? fn-name)
                  (str (gensym "fn-"))
                  fn-name))
    (keyword? sym) (name sym)
    :else (str sym)))

(defn state-disp-name
  [sym]
  (keyword (state-fn-name sym)))

(defn- next-emitted
  "Process events with the fsm-seq function f until we emit a new value for the sequence"
  [f]
  (when f
    (loop [[emitted next-step] (f)]
      (if next-step
        (if (not= :reduce-fsm.fsm/no-event emitted)
          [emitted next-step]
          (recur (next-step)))
        [emitted nil]))))

(defn ^{:skip-wiki true} fsm-seq-impl*
  "Create a lazy sequence from a fsm-seq state function"
  [f]
  (let [[emitted next-step] (next-emitted f)]
    (lazy-seq
      (if next-step
        (cons emitted (fsm-seq-impl* next-step))
        (when (not= :reduce-fsm.fsm/no-event emitted)
          (cons emitted nil))))))
