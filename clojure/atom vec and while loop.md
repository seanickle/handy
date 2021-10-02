### atom vec `[]` and while loop...
```clojure
(let [results-atom (atom [])]
    ;
    ; (println "elements: " (count @results-atom))
    ; (swap! results-atom conj "hi")
    ;results-atom
    (while
      (< (count @results-atom) 3)
      (do
        (println "doing")
        ; insert
        (swap! results-atom conj "hi")
        (println "elements: " (count @results-atom))
          ))
    ; done
    (println "Done. Now have elements: " (count @results-atom))
    )
```
* => 
```clojure
doing
elements:  1
doing
elements:  2
doing
elements:  3
Done. Now have elements:  3
nil
```
