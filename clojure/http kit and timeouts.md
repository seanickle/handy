### test `http-kit` timeout..
* look at result for a `timeout 1ms`..
```clojure
  (require
    ['org.httpkit.client :as 'http])

  (let
    [options {:timeout 1}
     url "http://yahoo.com"
     ]
    (def vout @(http/get url options)))

```
* ==>
```clojure
user=> (keys vout)
(:opts :error)
user=> vout
{:opts {:timeout 1, :method :get, :url "http://yahoo.com"}, :error #error {
 :cause "read timeout: 1ms"
 :via
 [{:type org.httpkit.client.TimeoutException
   :message "read timeout: 1ms"
   :at [org.httpkit.client.HttpClient clearTimeout "HttpClient.java" 82]}]
 :trace
 [[org.httpkit.client.HttpClient clearTimeout "HttpClient.java" 82]
  [org.httpkit.client.HttpClient run "HttpClient.java" 433]
  [java.lang.Thread run "Thread.java" 748]]}}
user=> (type (:error vout))
org.httpkit.client.TimeoutException
```

### Answer the question: does `http-kit` func use the callback if there is an `{:error ...}` ?
* ...
```clojure
  (let
    [options {:timeout 1}
     url "http://yahoo.com"
     callback-fn #(println "callback yay! ____" % "_____")
     ]
    ;(def vout @(http/get url options))
    ;(def vout @(http/get url options callback-fn))
    (def vout (http/get url options callback-fn))
    )
```
* => well looks like the _Callback_ func is still called on an error.
```clojure
callback yay! ____ {:opts {:timeout 1, :method :get, :url http://yahoo.com}, :error #error {
 :cause read timeout: 1ms
 :via
 [{:type org.httpkit.client.TimeoutException
   :message read timeout: 1ms
   :at [org.httpkit.client.HttpClient clearTimeout HttpClient.java 82]}]
 :trace
 [[org.httpkit.client.HttpClient clearTimeout HttpClient.java 82]
  [org.httpkit.client.HttpClient run HttpClient.java 433]
  [java.lang.Thread run Thread.java 748]]}} _____
user=> 
```
