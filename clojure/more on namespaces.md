### ? If on a repl, but wanting to simulate a namespace `ns` in a project file
*  using discussion in https://www.braveclojure.com/organization/#Anchor-3 ...
```clojure
(in-ns 'foo.my-test)

```
* hmm I thought that would give me access to the names in that namespace, but in my project, that didnt work...
* My namespace in .. has
```clojure
(ns foo.my-test
  (:require 
    [org.httpkit.client :as http]
    [org.httpkit.fake]))
```
* and when i tried ...
```clojure
user=> (in-ns 'foo.my-test)
#object[clojure.lang.Namespace 0x37b2f7ef "foo.my-test"]

foo.my-test=> (org.httpkit.fake/with-fake-http ["http://google.com/" "faked"
                              #_=>                  "http://flickr.com/" 500]
                              #_=>   (:body @(http/get "http://google.com/"))    ; "faked"
                              #_=>   (:status @(http/get "http://flickr.com/"))) ; 500

ClassNotFoundException org.httpkit.fake  java.net.URLClassLoader.findClass (URLClassLoader.java:381)
```

### Also initially confused with ...
* Starting new repl and cannot use `require` with raw library strings...
```clojure
user=> (require [org.httpkit.client :as http])

CompilerException java.lang.ClassNotFoundException: org.httpkit.client, compiling:(/private/var/folders/mj/7bwn1wld4pscycn91fpjn1h40000gn/T/form-init5251815666209634293.clj:1:1) 
```
* but `use` works. 
```clojure
user=> (use '[org.httpkit.client :as http])
WARNING: get already refers to: #'clojure.core/get in namespace: user, being replaced by: #'org.httpkit.client/get
nil
```
* Hmm even so, I am using `:as http`, so why is `use` replacing the `clojure.core/get` in this namespace? 

#### but using a fresh repl,  
* this worked:
```clojure

user=> (require ['org.httpkit.client :as 'http])
nil
user=> 

user=> (def v1 @(http/get "https://www.braveclojure.com/organization/"))
#'user/v1
user=> (keys v1)
(:opts :body :headers :status)
```
* So here, `'org.httpkit.client` is a symbol and not a variable like `org.httpkit.client` . 
