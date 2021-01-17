
### list tables, 
* first get the `faraday` class thing ready.

  ```
  boot.user=> (use '[taoensso.faraday :as dynamo])
  ```
* with a `client-config` hash defined in a separate `clj` file, here `one/dynamo.clj` is my file.

  ```
  (def client-config
    (if (:development env)
      {:access-key "OMGDEVELOPMENT"
       :secret-key "I_SHOULD_KEEP_THIS_SECRET!"

       ; Point the configuration at the DynamoDB Local
       :endpoint "http://localhost:8000"}

      {:endpoint "http://dynamodb.us-east-1.amazonaws.com"}
      )
  )
  ```
* and use `list-tables` from the module/class thing,

  ```
  boot.user=> (use '[one.dynamo :as db])    ; `one/dynamo.clj`

  boot.user=> (dynamo/list-tables db/client-config)
  (:primes :projects :times)
  ```

### create table 
* to get this `env` part to work, I didnt see any way to set the env vars in clojure, so I just set them on my shell,

  ```
  export development=true  # and this could have been anything that reduces to boolean true actually.
  ```
* and the dynamodb local credentials dont matter it turns out, 

  ```clojure
  ; borrowing this from source at https://github.com/jamesleonis/serverless-in-clojure
  ; , https://medium.com/@jamesleonis/clojure-in-aws-serverless-dynamodb-cd5ed29027a5#.u29ighn8s  
  ; 
  (def client-config
    (if (:development env)
      {:access-key "OMGDEVELOPMENT"
       :secret-key "I_SHOULD_KEEP_THIS_SECRET!"

       ; Point the configuration at the DynamoDB Local
       :endpoint "http://localhost:8000"}

      {:endpoint "http://dynamodb.us-east-1.amazonaws.com"} ; this is the else part.
      )
  )
  
  (def table-name :my_table)

  (dynamo/create-table client-config table-name
                         [:index :n] 
                         {:throughput {:read 5 :write 5}
                          :block? true})
  ```
  
* continuing to follow along from https://github.com/jamesleonis/serverless-in-clojure , 

  ```
  boot.user=> (use '[lesson-two.dynamo :as db] )  ; using some of the predefined functions from here,

  boot.user=> (db/list-primes)
  []
  boot.user=> (db/put-prime 0 2)
  nil
  boot.user=> (db/list-primes)
  [2]

  ```
