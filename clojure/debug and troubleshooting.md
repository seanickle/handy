### print data structures so as to preserve quotation (quotes)
* wow .. took too long to come across this nugget
* https://stackoverflow.com/questions/21136766/clojure-printing-functions-pr-vs-print
* `pr/prn` is to `print/println` for human readability.
```clojure
user=> (def d1 {:foo {:nil true :and "yay"}})
#'user/d1

user=> (prn "ok... " d1)
"ok... " {:foo {:nil true, :and "yay"}}
nil
```
