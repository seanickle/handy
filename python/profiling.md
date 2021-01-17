
#### line profiler
* Big fan of the [line_profiler](https://github.com/pyutils/line_profiler) ( formerly [here](https://github.com/rkern/line_profiler) ) 

```
pip install line_profiler
```

* `expensive.py`
```python
import time
  
@profile
def foo():
    for x in range(10):
        bar()
        flarg()
  
@profile
def bar():
    time.sleep(.1)
  
@profile
def flarg():
    time.sleep(.1)
  
foo()

```
* profile 

```python
(pandars38) ツ kernprof -lv  expensive.py
Wrote profile results to expensive.py.lprof
Timer unit: 1e-06 s

Total time: 2.06251 s
File: expensive.py
Function: foo at line 3

Line #      Hits         Time  Per Hit   % Time  Line Contents
==============================================================
     3                                           @profile
     4                                           def foo():
     5        11         54.0      4.9      0.0      for x in range(10):
     6        10    1027267.0 102726.7     49.8          bar()
     7        10    1035191.0 103519.1     50.2          flarg()

Total time: 1.02698 s
File: expensive.py
Function: bar at line 9

Line #      Hits         Time  Per Hit   % Time  Line Contents
==============================================================
     9                                           @profile
    10                                           def bar():
    11        10    1026983.0 102698.3    100.0      time.sleep(.1)

Total time: 1.0349 s
File: expensive.py
Function: flarg at line 13

Line #      Hits         Time  Per Hit   % Time  Line Contents
==============================================================
    13                                           @profile
    14                                           def flarg():
    15        10    1034899.0 103489.9    100.0      time.sleep(.1)



```
