
## from clojure for machine learnings..
```clojure
(defn plot-points
  "plots sample points of a solution s"
  [s]
  (let [X (concat (:hidden s) (:observed s))
        Y (concat (:hidden-values s) (:observed-values s))]
    (view     ; NOTE save instead of view can save to a file.
      (add-points
        (xy-plot X Y) (:observed s) (:observed-values s)))))
        
; namespace...
; [incanter "1.5.4"]
(ns my-namespace
  (:use [incanter.charts :only [xy-plot add-points]]
        [incanter.core :only [view]])
   (:require [clojure.core.matrix.operators :as M]
             [clatrix.core :as cl]))
             
             
(ns my-namespace
   (:use clojure.core.matrix)
   (:require [clatrix.core :as cl]))


; from csv , to matrix..
(with-open [reader (io/reader "in-file.csv")]
  (doall
    (csv/read-csv reader)))
    
    
             
```
