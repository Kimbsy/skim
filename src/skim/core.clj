(ns skim.core)

(def self-evaluating
  #{java.lang.String
    java.lang.Character
    java.lang.Long
    java.lang.Double
    java.lang.Boolean})

;; minimal initial environment
(def initial-env
  {'- -
   '= =
   '< <
   'nand (fn [a b] (not (and a b)))})

(declare evaluate)

(defn extend-env
  "Extend the environment by binding `names` to `values`.

  Evaluate expressions one by one, so we can refer to previous names
  in the same bind form."
  [env names values]
  (reduce (fn [e [n v]]
            (assoc e n (evaluate v e)))
          env
          (zipmap names values)))

(defn make-fn
  [args body env]
  (fn [& values]
    ;; this should be an `eprogn`, unless we only allow a single form as body?
    (evaluate body (extend-env env args values))))

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
                        (extend-env env names values)))

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
