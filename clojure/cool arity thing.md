### keys keywords from map 

```clojure

(let [
      {:keys [status headers body error]} {:status 0 :headers 1 :body 3 :error 5 :extra 88} 
      ]
  (println status body)
  )


your.app=> (let [
      #_=>       {:keys [status headers body error]} {:status 0 :headers 1 :body 3 :error 5 :extra 88} 
      #_=>       ]
      #_=>   (println status body)
      #_=>   )
0 3
nil
your.app=>
```

### also keyword args arity
```clojure
(comment
  ""
  (defn foof
    [a b & {:keys [op-fn]
            :or {op-fn +}}]
    (op-fn a b))

  (foof 4 5 :op-fn *)
  (foof 4 5 :op-fn #(str %1 ".." %2))

  )
```
