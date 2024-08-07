(ns skim.core)

(def self-evaluating
  #{java.lang.String
    java.lang.Character
    java.lang.Long
    java.lang.Double
    java.lang.Boolean
    clojure.lang.Keyword})

;; minimal initial environment
(def initial-env
  {'- -
   '= =
   '< <
   'nand (fn [a b] (not (and a b)))
   'cons cons
   'first first
   'rest rest
   'coll? coll?
   'symbol? symbol?})

(declare evaluate)

(defn extend-env-iterative-eval
  "Extend the environment by binding `names` to `values`.

  `value` expressions have not been evaluated as they may refer to
  previous `names`. Each must be evaluated, then used to extend the
  environment before the next can be evaluated."
  [env names values]
  (reduce (fn [e [n v]]
            (assoc e n (evaluate v e)))
          env
          (map list names values)))

(defn extend-env
  "Extend the environment by binding `names` to `values`.

  `value` expressions have already been evaluated."
  [env names values]
  (merge env
         (zipmap names values)))

(defn make-fn
  "Creates a function which has an implicit reference to itself bound to
  the symbol `recur`."
  [args body env]
  (let [fenv (atom env)
        f (fn [& values]
            (evaluate body (extend-env @fenv args values)))]
    (swap! fenv #(assoc % 'recur f))
    f))

(defn invoke
  [f args]
  (apply f args))

(defn evlist
  [l env]
  (map #(evaluate % env) l))

(defn evaluate
  ([e]
   (evaluate e initial-env))
  ([e env]
   (if (not (coll? e))
     ;; evaluate an atom
     (cond
       ;; self-evaluating
       (self-evaluating (type e)) e

       ;; lookup symbol
       (symbol? e) (get env e))
     ;; evaluate a list
     (case (first e)
       ;; quote
       quote (second e)

       ;; if
       if (if (evaluate (nth e 1) env)
            (evaluate (nth e 2) env)
            (evaluate (nth e 3) env))

       ;; lambda
       func (make-fn (nth e 1) (nth e 2) env)

       ;; let
       bind (let [[names values] (->> (nth e 1)
                                      (partition 2)
                                      (apply map list))]
              (evaluate (nth e 2)
                        (extend-env-iterative-eval env names values)))

       ;; default to function application
       (invoke (evaluate (first e) env)
               (evlist (rest e) env))))))

(def eval-string (comp evaluate read-string))

(defn eval-file
  [filename]
  (eval-string (slurp filename)))

(defn repl
  []
  (while true
    (newline)
    (print "> ")
    (flush)
    (prn (evaluate (read-string (read-line))))))

(defn -main
  [& args]
  (if args
    (prn (eval-file (first args)))

    (do
      (print "repl:\n")
      (flush)
      (repl))))
