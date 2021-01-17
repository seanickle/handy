### basic callbacks...
```clojure
(comment 
  "basic callback"
  (let [callback-fn #(+ 5 %)
        ]
    (callback-fn 10))
)

```
* ...
```clojure
user=>   (let [callback-fn #(+ 5 %)
  #_=>         ]
  #_=>     (callback-fn 10))
15
```

### callback w `future`
```clojure
(comment
  "future w callback func"
  (defn use-callback-when-done
    [callback-fn]
    (future (callback-fn (+ 4 5))))

  (def output (use-callback-when-done #(println "printings.. " % " .end")))
)
```
* => 
```clojure
user=> (def output (use-callback-when-done #(println "printings.. " % " .end")))
#'user/output
printings..  9  .end
user=> 
```

### callback and core async...
```clojure
(comment
  "use a go put onto a channel callback..."
  (defn use-callback-when-done
    [callback-fn]
    (future (callback-fn (+ 4 5))))
  
  (require 
    ['clojure.core.async
     :refer ['>! '<! '>!! '<!! 'go 'chan
             'alts! 'alts!! 'timeout]])
  (let [
        my-results-chan (chan)
        callback-fn #(go (>! my-results-chan %))
        output (use-callback-when-done callback-fn)
        ]
    ; (fn [x] (<!! my-results-chan))
    (println "looking at the channel: " (<!! my-results-chan))
    (println "done..")
    )
)

```
* ==>
```clojure
user=>   (let [
  #_=>         my-results-chan (chan)
  #_=>         callback-fn #(go (>! my-results-chan %))
  #_=>         output (use-callback-when-done callback-fn)
  #_=>         ]
  #_=>     ; (fn [x] (<!! my-results-chan))
  #_=>     (println "looking at the channel: " (<!! my-results-chan))
  #_=>     (println "done..")
  #_=>     )
looking at the channel:  9
done..
nil
user=>
```
