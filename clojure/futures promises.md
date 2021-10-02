### sleeping...
```clojure
(Thread/sleep 4000)
```

### simple multithreading  , from the Brave clojure book
```clojure
(future (Thread/sleep 4000)
        (println "I'll print after 4 seconds"))
(println "I'll print immediately")
```

### hmm this is weird. so dereferencing the future blocks? 
```clojure
(defn fight-crime
  []
  (let []
  (println "hi")
  (Thread/sleep 2000)
  (println "ho")
  (Thread/sleep 1000)
  (println "yo")
  5
  ))

(let [result (future (fight-crime))]
  (println "@: " @result)
  (println "snore. " )
  (println "@: " @result)
  (Thread/sleep 1000)
  (println "@: " @result)
)
```
* Ah ok, but you can stop waiting.. so here we wait `10ms` and then return "hmmf" if future isnt done yet.
```clojure
(let [result (future (fight-crime))]
  (println "@: " (deref result 10 "hmmf"))
  (println "snore. " )
  (println "@: " (deref result 10 "hmmf"))
  (Thread/sleep 5000)
  (println "@: " (deref result 10 "hmmf"))
)
```

### delays. cool
```clojure
(def jackson-5-delay
  (delay (let [message "Just call my name and I'll be there"]
           (println "First deref:" message)
           message)))
```
* and a colorful example with `future`,  `force`, `delay` , from http://www.braveclojure.com/concurrency/
```clojure
(def gimli-headshots ["serious.jpg" "fun.jpg" "playful.jpg"])
(defn email-user
  [email-address]
  (println "Sending headshot notification to" email-address))
(defn upload-document
  "Needs to be implemented"
  [headshot]
  true)
(let [notify (delay (email-user "and-my-axe@gmail.com"))]
  (doseq [headshot gimli-headshots]
    (future (upload-document headshot)
            (force notify))))
```

### Blocking thread in action...
```clojure
; make a future which blocks on a promise. Then deliver promise in main thread and see what happens.
(def a-promise (promise))
(def futr
    (future 
        (def result (+ @a-promise 10))
        (Thread/sleep 5000)
        (println "result " result)
        result))
; 
(println "nothing yet: " (deref futr 10 "nothing."))
; deliver..    
(deliver a-promise 99)
(println "right after deliverin " (deref futr 10 "still nothin."))
(Thread/sleep 5500)
(println "had some time to think... " (deref futr))
```

### delivering to promise multiple times?
* hmm, how about the other way around... make some workers do something and have them all try deliver same promise. What will happen?
```clojure
(def another-promise (promise))
(def d1 (delay 
                (Thread/sleep 6000)
                (deliver another-promise 6000)
                ))
;
(def d2 (delay 
                (Thread/sleep 4000)
                (deliver another-promise 4000)
                ))
;
(def d3 (delay 
                (Thread/sleep 1000)
                (deliver another-promise 1000)
                ))
; nothing there right?
(realized? another-promise)
; now run them all...
(doseq [a-worker [d1 d2 d3]]
        (future (force a-worker))
        )

(println "Check promise: " (deref another-promise 10 "nothin."))
(println "Check promise: " @another-promise)
```
* hmm, for the above, strange that I tried but was not able to `deref` the delays. Error was 
```clojure
ClassCastException clojure.lang.Delay cannot be cast to java.util.concurrent.Future  clojure.core/deref-future (core.clj:2206)
```
* 

### exercise in http://www.braveclojure.com/concurrency/
* given the yak butter search data...
```clojure
(def yak-butter-international
  {:store "Yak Butter International"
    :price 90
    :smoothness 90})
(def butter-than-nothing
  {:store "Butter Than Nothing"
   :price 150
   :smoothness 83})
;; This is the butter that meets our requirements
(def baby-got-yak
  {:store "Baby Got Yak"
   :price 94
   :smoothness 99})

(defn mock-api-call
  [result]
  (Thread/sleep 1000)
  result)

(defn satisfactory?
  "If the butter meets our criteria, return the butter, else return false"
  [butter]
  (and (<= (:price butter) 100)
       (>= (:smoothness butter) 97)
       butter))
```
* The original non concurrent code was ...
```clojure
(time (some (comp satisfactory? mock-api-call)
            [yak-butter-international butter-than-nothing baby-got-yak]))
; => "Elapsed time: 3002.132 msecs"
; => {:store "Baby Got Yak", :smoothness 99, :price 94}
```
* and a concurrent version...
```clojure
(defn blah-func
    []
    (def best-butter-promise (promise))
    (time
        (doseq [some-butter [yak-butter-international butter-than-nothing baby-got-yak]]
            (future (if ((comp satisfactory? mock-api-call) some-butter)
                        (deliver best-butter-promise some-butter)
                        )))
                        )
    (time
        (println "result is: " @best-butter-promise))
    )
(blah-func)

=>
cloj-multiproc-play.core=> (blah-func)
"Elapsed time: 0.615436 msecs"
result is:  {:store Baby Got Yak, :price 94, :smoothness 99}
"Elapsed time: 1001.655823 msecs"
nil
cloj-multiproc-play.core=>
```

### one more example , racing Bing vs Google
```clojure

(def search-result-promise (promise))

(def bingurl  "https://www.bing.com/search?q=foobar")

(def googleurl "https://www.google.com/?gws_rd=ssl#q=foobar")

(doseq [url [bingurl googleurl]]
    (future (deliver search-result-promise (slurp url)
       ))
)

(def html1 (slurp "https://www.google.com/?gws_rd=ssl#q=foobar"))

```

### hot dog machine from http://www.braveclojure.com/core-async/
* my version of a hot dog machine that only dispenses max number of hot dogs...
```clojure
(defn hot-dog-machine-v2
  [how-many-dogs-init]
  (let [in (chan)
        out (chan)]
    (go
      (loop [num-dogs how-many-dogs-init]
        ; if no hot dogs left, then done.
        (if (= num-dogs 0)
          true
          ; Otherwise 
          (do
            (println "Going to <! block on <in> now.")
            (<! in)
            (println "Now we have " num-dogs " dogs.")
            (println "Going to >! block on <out> now.")
            (>! out "hot dog")
            ))
        (recur (dec num-dogs))
        
        ))
    [in out]
    ))

; get channels..
(def machine-channels (hot-dog-machine-v2 5))
(def in (first machine-channels))
(def out (last machine-channels))

; ... dispensing them    
(println "one")
(>!! in "pocket lint")
(<!! out)

(println "two")
(>!! in "pocket lint")
(<!! out)

(println "three")
(>!! in "pocket lint")
(<!! out)

(println "four")
(>!! in "pocket lint")
(<!! out)

(println "five")
(>!! in "pocket lint")
(<!! out)
....

; that was fine except we went into negative hot dogs...
playsync.core=> (println "five")
five
nil
playsync.core=> (>!! in "pocket lint")
trueNow we have  1  dogs.
Going to >! block on <out> now.

playsync.core=> (<!! out)
Going to <! block on <in> now.
"hot dog"
playsync.core=> 

playsync.core=> (>!! in "pocket lint")
Now we have  -1  dogs.
true
Going to >! block on <out> now.
playsync.core=> (<!! out)
"hot dog"Going to <! block on <in> now.

```
* .. update
```clojure
(let [results-chan (chan)
    results-vector (atom [])]
    
    ; For several things in stuff-vec , start some async call that will throw results,
    ; into the results-chan
    (doseq [x stuff-vec]
        (do-something-async results-chan x))
        
    ; in this case, we take from the results-chan once they are ready, 
    ; and update (using conj) the atom results-vector
    (doseq [_ stuff-vec]
        (swap! results-vector conj (<!! results-chan)))
    )
```

### swap syntax for updating atoms...
* 
