#### enter and exit
* According to [stack overflow](https://stackoverflow.com/questions/1984325/explaining-pythons-enter-and-exit) you can..


```python

In [97]: class Blah(object):
    ...:     def __enter__(self):
    ...:         print("hello")
    ...:         return self
    ...:     def __exit__(self, exc_type, exc_val, exc_tb):
    ...:         print("bye!")
    ...:     my = "stuff"
    ...: 

In [98]: with Blah():
    ...:     print("doing stuff")
    ...: 
hello
doing stuff
bye!
```

* The specific example that follows this is the object which is returned by 
 
```python
import psycopg2
conn = psycopg2.connect()
```
```python
Help on connection object:

class connection(builtins.object)
 |  connection(dsn, ...) -> new connection object
 |  
 |  :Groups:
 |    * `DBAPI-2.0 errors`: Error, Warning, InterfaceError,
 |      DatabaseError, InternalError, OperationalError,
 |      ProgrammingError, IntegrityError, DataError, NotSupportedError
 |  
 |  Methods defined here:
 |  
 |  __enter__(...)
 |      __enter__ -> self
 |  
 |  __exit__(...)
 |      __exit__ -- commit if no exception, else roll back
 |  


```
