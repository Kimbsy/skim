# skim

A toy Lisp written in Clojure.

# Usage

You can run a repl with:

``` bash
./skim
```

or evaluate a program with:

``` bash
./skim example.skm
```

# Syntax

Pretty straightforward Lisp syntax, our special forms are `quote`, `if`, `func` and `bind`.

Clojure is our definitional language so comments are preceded with any number of semicolons `;`.

``` Clojure
(- 4 1)
;; 3

(quote (foo bar baz))
;; (foo bar baz)

(if (= 3 4)
  "same"
  "not same")
;; "not same"

(bind (a 1
       b 13)
      (- b a))
;; 12

(bind (minus-5 (func (n)
                     (- n 5)))
      (minus-5 20))
;; 15

```

# Initial environment

We have an intentionally minimal initial environment, the only functions that exist as part of the language definition are `-`, `=`, `<` and `nand` as these are sufficient to implement most required arithmetic or boolean functions (see `examples/core.skm`).

# Recursion

Inside the lambda form `func` is an implicitly available binding `recur`.

``` Clojure
;; need to define `+` first
(bind (+ (func (a b)
               (- a (- b)))

       ;; finding Fibonnaci numbers with multiple recursive calls
       fib (func (n)
                 (if (< n 2)
                   n
                   (+ (recur (- n 1))
                      (recur (- n 2))))))

      (fib 10))
;; 55
```

# TODO:

- think about lists, conj/cons, car/cadr/first/rest etc.
- can we define a `core.skm` with common functions written in skim? Would need the ability to import libraries? some kind of `ns`-like special form?
- can we create an emacs mode with correct indentation rules?
