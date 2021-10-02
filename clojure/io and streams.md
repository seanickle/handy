
### Amazonica and s3
* This was not super obvious, because this example uses a `java.io.ByteArrayInputStream`
with the the `:input-stream` parameter of the `put-object` function
* But in my mind this feels more like an output stream since we're writing. 
* but maybe this is because we're reading from the `payload` .
```clojure
(require ['amazonica.aws.s3 :as 'ss3])

(defn put-s3-obj
  [bucket-name s3key content]
  (let [payload (.getBytes content "UTF-8")
        input-stream (java.io.ByteArrayInputStream. payload)]
    (ss3/put-object :bucket-name bucket-name
                    :key s3key
                    :input-stream input-stream
                    ; :metadata {:server-side-encryption "AES256"} ;?
                    ;:file content
                    )))
                    
```
