
#### I am used to python's zip
```python
zip([1, 2, 3], ['a', 'b', 'c'])
# [(1, 'a'), (2, 'b'), (3, 'c')]
```

#### Interleaving and partitioning can do the same thing
* From [stackoverflow](https://stackoverflow.com/a/17806931/472876) , below, 
* This is so clever ...
```clojure


(partition 2 (interleave '(1 2 3) '(4 5 6))) 
; => ((1 4) (2 5) (3 6))

; or more generally

(defn zip [& colls]
  (partition (count colls) (apply interleave colls)))

(zip '( 1 2 3) '(4 5 6))           ;=> ((1 4) (2 5) (3 6))

(zip '( 1 2 3) '(4 5 6) '(2 4 8))  ;=> ((1 4 2) (2 5 4) (3 6 8))

```

#### This was also a cool solution
* From [here](https://stackoverflow.com/a/2588385/472876)
```clojure
user=> (map vector [1 2 3] [4 5 6])
([1 4] [2 5] [3 6])
user=> 

```
