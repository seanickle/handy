### hmm... using `is`
* so the library is built in, but you still have to start `use`ing it..
```clojure
boot.user=> (is (= 4 (+ 2 2)))

             java.lang.RuntimeException: Unable to resolve symbol: is in this context
clojure.lang.Compiler$CompilerException: java.lang.RuntimeException: Unable to resolve symbol: is in this context, compiling:(/var/folders/7_/sbz867_n7bdcdtdry2mdz1z00000gn/T/boot.user2780891586981282255.clj:1:1)
boot.user=> 

boot.user=> 

boot.user=> (use 'clojure.test)
nil
boot.user=> (is (= 4 (+ 2 2)))
true
```

### lein test 
* Running all tests in a file
```
lein test module/blah/test_file.py
```
* Running specific `deftest` in `module_hmm/blah/test_file.py` called `test-foo`
```
lein test :only module-hmm.blah.test-file/test-foo
```
