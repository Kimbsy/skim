(ns skim.core)

(def self-evaluating
  #{java.lang.String
    java.lang.Character
    java.lang.Long
    java.lang.Double
    java.lang.Boolean})

(def initial-env
  {'foo 4
   '+ +
   '- -
   '= =
   '< <})

(declare evaluate)

(defn extend-env
  [env names values]
  (reduce
   (fn [e [n v]]
     (assoc e n v))
   env
   (zipmap names values)))

(defn make-fn
  [args body env]
  (prn "make-fn")
  (newline)
  (fn mult [& values]
    (prn "custom-fn-env")
    (prn (extend-env env args values))
    (newline)
    ;; this should be an `eprogn`, unless we only allow a single form as body?
    (evaluate body (extend-env env args values))))

(defn invoke
  [f args]
  (prn "invoke")
  (prn f args)
  (newline)
  (apply f args))

(defn evlist
  [l env]
  (prn "evlist")
  (prn l env)
  (newline)
  (map #(evaluate % env) l))

(defn evaluate
  ([e]
   (evaluate e initial-env))
  ([e env]
   (if (not (coll? e))
     (cond
       ;; self-evaluating
       (self-evaluating (type e)) e

       ;; lookup symbol
       (symbol? e) (get env e)

       :else :NONE-ATOM)
     (cond
       ;; quote
       (= 'quote (first e)) (second e)

       ;; if
       (= 'if (first e)) (if (evaluate (nth e 1) env)
                           (evaluate (nth e 2) env)
                           (evaluate (nth e 3) env))

       ;; lambda
       (= 'func (first e)) (make-fn (nth e 1) (nth e 2) env)

       ;; let
       (= 'bind (first e)) (let [[names values] (->> (nth e 1)
                                                     (partition 2)
                                                     (apply map list))]
                             (evaluate (nth e 2)
                                       (extend-env
                                        env
                                        names
                                        (evlist values env))))

       ;; apply function
       (seq e) (invoke (evaluate (first e) env)
                       (evlist (rest e) env))

       :else :NONE-COLL))))

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
