
(Porting over my notes from gists .. [here](https://gist.github.com/namoopsoo/607f29e923ceaba890588e69293413cf) ),..

### quick note on imports
* i originally inferred this 
```clojure
(ns blah-namespace
  (:use clojure.core.blah )  ; this would take all the terms in blah and put them into the current namespace

  (:refer-clojure :exclude [blarg flarg]) ; but this is supposed to be a way to avoid term clash
                                          ; so blarg and flarg will not be used. (not that they are in clojure.core however)
  )
```
* Then I read  https://www.braveclojure.com/organization/#Anchor-3 , and read `use` is a shortcut 
  for `require` followed by a `refer`. 

### hash-maps
* Many ways to make a hash-map,

  ```
  ; These are both fine
  boot.user=> {:name "Steve" :age 24 :salary 7886 :company "Acme"}
  {:name "Steve", :age 24, :salary 7886, :company "Acme"}
  boot.user=> {:name "Steve", :age 24, :salary 7886, :company "Acme"}
  {:name "Steve", :age 24, :salary 7886, :company "Acme"}
  ```
* how to get a value from the hash-map...  

  ```  
  boot.user=> (steve "age")  ; no
  nil
  boot.user=> (steve 24) ; no. not even sure what this is trying to do
  nil
  boot.user=> (steve :company) ; yes
  "Acme"
  boot.user=> (steve :name) ; yes
  "Steve"
  ```
* what is insane is that this is also legal... and I see this one used more often:

  ```
  boot.user=> (:name steve)
  "Steve"
  ```
* and `get` too..

  ```
  boot.user=> (get steve :company)
  "Acme"
  ```
  
* accessing nested hashes...

  ```clojure
  app.core=> (get-in {:foo {:and "yes" :here "no"}} [:foo :here])
  "no"
  ```
  
### Array things
* similarly can index arrays like hash-maps...

  ```
    boot.user=> (def a1 [1 2 3 4])
    #'boot.user/a1
    boot.user=> a1
    [1 2 3 4]
    boot.user=> (first a1)
    1
  ```
  
### Boot-repl
* According to https://github.com/boot-clj/boot , you can set environmental variables ...

  ```
  boot.user=> (set-env! 
       #_=>   :resource-paths #{"src"}
       #_=>   :dependencies '[[me.raynes/conch "0.8.0"]])
  ```
* but this is not working for me or at least not for the REPL i'm in, 

  ```
  boot.user=> (:development env)
  nil
  boot.user=> (set-env!
         #_=> :development true)
  nil
  boot.user=> 

  boot.user=> (:development env)
  nil
  ```

* And to look at one or more of what is in `env` 

  ```
  boot.user=> (use '[environ.core :as env])
  nil
  
  ; now you can look at anything with 
  boot.user=> (env :term)
  "xterm-256color"
  ```
  
  * and clojure re-writes envrironmental variables, changing `CAPITAL_LETTERS` into `capital-letters`,
  * so if on shell one did `export CAPITAL_LETTERS=bingo`
  * Then `environ.core` could get this with `(env :capital-letters)`

### lambda functions in clojure ( aka anonymous functions)
* the `#(...)` 

  ```
  boot.user=> (#(+ %1 5) 2)
  7
  boot.user=> (#(+ %1 5) 2 3)

  clojure.lang.ArityException: Wrong number of args (2) passed to: user/eval5691/fn--5692
  boot.user=> (#(+ %1 5 %2) 2 3)
  10
  ```
* actually, another nicer looking lambda form. I like this more..

  ```
  boot.user=> (map (fn [x] (x :name)) [{:name "Jo"},{:name "Jane"},{:hmm "Yea"}])
  ("Jo" "Jane" nil)
  boot.user=> 
  ```
* anonymous func approach to collecting keys from hash...

  ```clojure
  app.core=> ((fn [{:keys [status error]}] (println status error)) {:status 400 :error false})
  400 false
  nil
  ```

### Another note on `conj` and hashes...
* conj hashes, 
```clojure
app.core=> (def blah {"acc" "123"})
#'app.core/blah
app.core=> blah
{"acc" "123"}

app.core=> (conj blah {"secret" "455"})
{"acc" "123", "secret" "455"}
app.core=> (conj blah {"secret" "455" "client_id" "5667"})
{"acc" "123", "secret" "455", "client_id" "5667"}
```

### a weird way of calling a func using `apply`, and some strange dereferencing thing ,
* Best quote about `apply` i read is ...
  > apply is used to apply an operator to its operands.

* so `apply` takes a `func` as its first arg and then throws whatever is next , 1 or more args, to `func`. 
* I Guess that's a lie, because the thing is though, you want to give `func` a list thing.

  ```
  boot.user=> (max 1 2 3 4 )
  4
  boot.user=> (apply #(+  %1 %3) '[4 5 6 ])
  10
  ```
* but all of these forms seem to work which is weird

  ```
  boot.user=> (apply #(+  %1 %3) [4 5 6] )
  10
  boot.user=> (apply #(+  %1 %3) `[4 5 6] )
  10
  boot.user=> (apply #(+  %1 %3) '[4 5 6] )
  10
  
  
  ```
  
### importing from other files...
* for a file  `"src/lesson_two/dynamo.clj"` , `use` will let the namespace in it referred to as `db`

  ```
  boot.user=> (load-file "src/lesson_two/dynamo.clj")
  #'lesson-two.dynamo/get-prime
  boot.user=> (use '[lesson-two.dynamo :as db])
  nil
  ```
  
### Reload mechanism...  
* reload  after making changes if using `use`
  * from http://stackoverflow.com/questions/7658981/how-to-reload-a-clojure-file-in-repl#20337925 
  
  ```clojure
  (use 'your.namespace :reload)
  ```
* When using a `lein repl`, the above was not working for some reason, but this pattern worked for me...
  * pull up a REPL.. 
  
    ```clojure
    app.core=> (require '[app.core :as mycore])

    app.core=> (mycore/blah "hi")
    ```
  * edit code ...
  * then in the REPL...
  
    ```clojure
    app.core=> (require '[app.core] :reload)
    nil

    app.core=> (mycore/blah "hi")
    ```
  * and `mycore/blah` is available after the _reload_ with any changes taking effect.
  * but also this was also good , `reload-all` ... not sure about the difference definitively 
  
    ```clojure
    (require 'app.core :reload-all)
    ```
* Also, for the namespace the _repl_ itself is in, this worked to reload that..

  ```clojure
  (use 'app.core :reload-all)
  ```
  
### interesting way of checking types in a hash map
* throw error if wrong type

  ```
  boot.user=> (def steve {:name "Steve", :age 24, :salary 7886, :company "Acme"})
  #'boot.user/steve
  boot.user=> 

  boot.user=> steve
  {:name "Steve", :age 24, :salary 7886, :company "Acme"}
  boot.user=> 

  boot.user=> (-> steve :age num)
  24
  boot.user=> (-> steve :company num)

  java.lang.ClassCastException: java.lang.String cannot be cast to java.lang.Number

  boot.user=> (-> steve :company str)
  "Acme"

  ```
  
### string splitting
* requires an import 
  
  ```
  boot.user=> (use '[clojure.string :only (split triml)])
  nil
  boot.user=> (split "blarmey ; klarg ; jlarf ; mlarg" #";")
  ["blarmey " " klarg " " jlarf " " mlarg"]
  ```
* and trimming white space and beginning, end of a string,

  ```
  boot.user=> (clojure.string/trim " a sdfd ")
  "a sdfd"
  ```
  
### Exception handling
* casting exception

  ```
  boot.user=> steve
  {:name "Steve", :age 24, :salary 7886, :company "Acme"}
  boot.user=> (try (-> steve :company num) (catch java.lang.ClassCastException ex "nope"))
  "nope"
  ```
* useful technique for capturing problems with the `NullPointerException`. Sometimes I will get one without explanation...
* from, https://stackoverflow.com/questions/10529063/getting-clojure-stacktrace
*  using ... clojure.repl.pst
```clojure
user=> (try (/ 1 0) (catch Exception e (pst e)))
ArithmeticException Divide by zero
    clojure.lang.Numbers.divide (Numbers.java:156)
    clojure.lang.Numbers.divide (Numbers.java:3691)
    user/eval28 (NO_SOURCE_FILE:8)
    clojure.lang.Compiler.eval (Compiler.java:6511)
    clojure.lang.Compiler.eval (Compiler.java:6477)
    clojure.core/eval (core.clj:2797)
    clojure.main/repl/read-eval-print--6569 (main.clj:245)
    clojure.main/repl/fn--6574 (main.clj:266)
    clojure.main/repl (main.clj:266)
    clojure.main/repl-opt (main.clj:332)
    clojure.main/main (main.clj:427)
    clojure.lang.Var.invoke (Var.java:423)
```

### Another Exception handling and or tracing technique,
* using `org.clojure/tools.trace`
```clojure
user=> (dotrace [list?]
  #_=> (do
  #_=>  (list? [])
  #_=>  (list? '(1 2 3))
  #_=>  (list?)
  #_=>  (list? (defn f [] (do ())))
  #_=>  (list? "a"))
  #_=> )
IllegalStateException Can't dynamically bind non-dynamic var: clojure.core/list?
  clojure.lang.Var.pushThreadBindings (Var.java:353)
```
* , 
```clojure
user=> (dotrace [list?]
  #_=>   (do
  #_=>   (list? [])
  #_=>   (list? '(1 2 3))
  #_=>   (list?)
  #_=>   (list? (defn f [] (do ())))
  #_=>   (list? "a")))
TRACE t1216: (list? [])
TRACE t1216: => false
TRACE t1217: (list? (1 2 3))
TRACE t1217: => true
TRACE t1218: (list?)
ArityException Wrong number of args (0) passed to: core$list-QMARK-  
  clojure.lang.AFn.throwArity (AFn.java:437)
```
  
### The arrow `->` 
* this example says it all.

  ```clojure
  
  (def c 5)
  ;; => #'user/c

  (-> c (+ 3) (/ 2) (- 1))                          
  ;; => 3

  ;; and if you are curious why
  (use 'clojure.walk)
  ;; => nil

  (macroexpand-all '(-> c (+ 3) (/ 2) (- 1)))
  ;; => (- (/ (+ c 3) 2) 1)
  ```
* The `->>` is also used. Have not yet differentiated between the two.

### vectors and lists 
* with a vector, like `["a" "b" "c"]`, you can index, like 

  ```
  boot.user=> (["a" "b" "c"] 1)
  "b"
  ```
* but apparently, vectors are difficult to modify. 
* Whereas , a list `'(1 2 3)` can have easier appending, but it is harder to index. `nth` on a list will be a  `O(n)` operation as opposed to `O(1)` on a vector.

  ```
  (nth (list 1 2 3 4 5) 3)
  ```
* reference: http://stackoverflow.com/a/11505188/472876
* Also..  
  
  > "Lists logically grow at the head, while vectors logically grow on the tail. You can see this in action when using the conj function. It will grow the collection according to the type of collection given to it. While you can grow collections on either side, it is performant to do so in this way."
  
### group by , wow this is cool
* just like what you expect from a standard `group-by`, and works out of the box, http://stackoverflow.com/a/9089403/472876

  ```
  (group-by #(select-keys % [:a :b]) vector-of-hashes)
  ```
* and a group-by followed by an aggregate, good example, http://stackoverflow.com/questions/36139680/clojure-aggregate-and-count-in-maps
  http://stackoverflow.com/a/36140333/472876
  * this particular example has a hash map in `DATA` like 
  ```
  [
    {
      "a": "X",
      "b": "M",
      "c": 188
    },
    {
      "a": "Y",
      "b": "M",
      "c": 165
    },
    {
      "a": "Y",
      "b": "M",
      "c": 313
    },
    {
      "a": "Y",
      "b": "P",
      "c": 188
    }
  ]
  ```
  * and uses a basic `count`, but any kind of `reduce` style func can be applied.
  
  ```
  (into {}
    (map       

      ; f       
      (fn [ [k vs] ] ;[k `unique count`]
        [k (count (into #{} (map #(get % "b") vs)))]) 

      ; coll
      (group-by #(get % "a") DATA)))
  ```

### `frequencies` is like the pythonic `collections.Counter`
* Get counts nicely.

### nice way to extract partial hash map , based on keys
* using something that reminds me of  perl  for some reason,

  ```
  boot.user=> (def h {:time-length 15N, :core-category "work", :project-identifier "proj-1", :sub-category "code"} ) 
  boot.user=> ( #(select-keys % [:core-category :project-identifier]) h)
  {:core-category "work", :project-identifier "proj-1"}

  ```

### was missing this ...
* take a few items at a time with `partition`,
```clojure

```

### Strange detail about clojure repl , namespaces and filenames,
* looks like namespaces with hypens, expect clojure files with underscores, 
* My file is `one/date-utils.clj` and I tried to use it like so, but I got this message.
```
boot.user=> (use '[one.date-utils :as mydateutils])

java.io.FileNotFoundException: Could not locate one/date_utils__init.class or one/date_utils.clj on classpath. Please check that namespaces with dashes use underscores in the Clojure file name.
boot.user=> 
```
* Then i changed the file to `one/date_utils.clj` and problem solved.
```
boot.user=> (use '[one.date-utils :as mydateutils])
nil
```

### `concat` vs using `concat` with `apply` 
* As the docs https://www.conj.io/store/v1/org.clojure/clojure/1.8.0/clj/clojure.core/concat  make note of, using just `concat` does not always have the intuitive behavior,
```clojure
boot.user=> (concat [[5 6 7] [2 1 2]])
([5 6 7] [2 1 2])
boot.user=> (apply concat [[5 6 7] [2 1 2]])
(5 6 7 2 1 2)
boot.user=>
```
* Basically `concat` takes sequences, and not a sequence of sequences
* And `(apply f args)` has the power to take a sequence `args` and hand its individual components
    to `f` as bare parameters.
* The first example was initially unintuitive, because I was trying to `concat` the output of a `map`, 
```clojure
(def full-list (concat
                (map (fn [x] (blah x))
                 input-vector)))
```
* that was not working, and so i needed to update w/ an apply,
```clojure
(def full-list (apply concat
                (map (fn [x] (blah x))
                 input-vector)))
```
* Similarly to `concat`, `conj` can be used with `apply` in the same way...
```clojure
app.core=> (def vec [0 1 2 3 4])
#'app.core/vec
app.core=> (apply conj vec [99 88])
[0 1 2 3 4 99 88]
```
* So to explain the above, the normal form for `conj` is actually `(conj vec 99 88)`, 
but if we have those args as a vector `[99 88]` then we can use `apply` to make the args vector
be presented to `conj` as args.

### `doseq` and `for`
* from ... https://stackoverflow.com/a/4725502/472876
_The difference is that for builds a lazy sequence and returns it while doseq is for executing side-effects and returns nil._
```clojure
user=> (for [x [1 2 3]] (+ x 5))
(6 7 8)
user=> (doseq [x [1 2 3]] (+ x 5))
nil
user=> (doseq [x [1 2 3]] (println x))
1
2
3
nil
```
* _If you want to build a new sequence based on other sequences, use for. If you want to do side-effects (printing, writing to a database, launching a nuclear warhead, etc) based on elements from some sequences, use doseq._

### Feels like `comp` is one of the identities of clojure in that it helps keep you using simple functions
* The order of operations is just like in _maths_ ... `(fºgºh)(x) := f(g(h(x)))`
```clojure
play.core=> ((comp #(- 4 %) #(* % 9) #(+ % 95)) 5)  
-896
```

### Another way to combine , merge maps..
* This feels like a very particular solution
```clojure
playsync.core=> (def m1 {:foo 1 :boo 0})
#'playsync.core/m1
playsync.core=> (def m2 {:foo 2 :yoo 0})
#'playsync.core/m2
playsync.core=> 

playsync.core=> (merge-with + m1 m2)
{:foo 3, :boo 0, :yoo 0}
```
