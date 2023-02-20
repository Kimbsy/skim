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
(+ 1 1)
;; 2

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

(bind (plus-5 (func (n)
                    (+ n 5)))
  (plus-5 20))
;; 25

```

# TODO:

- recursion, the body of a function is evaluated in an environment that doesn't know about it
- can we define a `core.skm` with common functions written in Skim? Would need the ability to import libraries? some kind of `ns`-like special form?
