#### logging,
* https://github.com/futurice/timbre
```clojure
; require...  
[taoensso.timbre :as log]

; i have ended up using it like this, in a let, with fake variables,
(let 
[
var1 (myfunc "blah")
fake1 (log/info (str "var1: " var1))]
() ; do stuff)


```
