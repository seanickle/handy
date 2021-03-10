### What the what
Notes from after converting a project using the `2to3`, of additional _gotchas_

### TOC
* [StringIO](#stringio)
* [Pickling](#cpickle-and-pickle)
* [Uuid](#uuid)
* [xrange](#xrange)
* [wow the silent division bug!](#partition-code-bug)
* [func.func_name](#no-more-func-name)
* calling lambdas w/ boto3 and using `BytesIO` 
* [Bytes and json](#bytes-and-json)
* lambda , `[ERROR] Runtime.MarshalError: Unable to marshal response: b'gAN9cQAoWA4`
* [dict merging](#dict-merging) 

### Meat

#### StringIO
* Doing this fixes things typically..
* Change
```python
import StringIO
```
* to 
```python
try:
    from StringIO import StringIO
except:
    from io import StringIO
```
* And update any `StringIO.StringIO()` to just `StringIO()`


#### cPickle and pickle
* Because theres no more `cPickle`
* I changed `cPickle` to `pickle` and started getting this 
```python
    226     with open(fn) as fd:
--> 227         dtypes_dict = pickle.load(fd)
    228         return dtypes_dict
    229 


TypeError: a bytes-like object is required, not 'str'
```
* because pickled objects encoded with  the string like protocol need to be re-encoded I think.
* But I was able to actually read the python2 ASCII pickle by doing this. Worked for me
```python
with open(fn,'rb') as fd:
    dtypes_dict = pickle.load(fd)
```

#### Treating `somedict.keys()` as a list 
```python
In [32]: dtypes_dict.keys()[:5]                                                         
---------------------------------------------------------------------------
TypeError                                 Traceback (most recent call last)
<ipython-input-32-41046874d947> in <module>
----> 1 dtypes_dict.keys()[:5]

TypeError: 'dict_keys' object is not subscriptable
```
* I think just need change to this... 
```python
list(dtypes_dict.keys())[:5]
```

#### uuid
* Got this.
```python
     43 def make_nonce():
---> 44     return uuid.uuid4().get_hex()
     45 
     46 def make_date_s3_prefix(timestamp):

AttributeError: 'UUID' object has no attribute 'get_hex'

```
* Changed to ...
```python
In [12]: uu.hex                                                                         
Out[12]: '19487abb29fb4e8197df6f000c31b358'


```

#### `xrange`
* no more `xrange`.
* it's now just `range`  
* note per [here](https://treyhunner.com/2018/02/python-3-s-range-better-than-python-2-s-xrange/)


#### Partition code bug
* This func didnt crash in python 3 but the result was quite different.
```python
def get_partitions(vec, slice_size):
    assert slice_size > 0
    assert isinstance(vec, list)
    num_slices = int(math.ceil(len(vec)/slice_size))
    size_remainder = len(vec) - num_slices*slice_size
    slices = [vec[k*slice_size:k*slice_size+slice_size] for k in range(num_slices)]

    if size_remainder:
        slices.append(vec[-(size_remainder):])

    return slices
```
* python 2: as expected
```python
ids = [2220706, 2220705, 2220703, 2220700, 2220696, 2220690, 2220688, 2220687, 2220682, 2220676, 2220674, 2220671]
# len(ids) # 12
get_partitions(ids, 5)
# => 
[[2220706, 2220705, 2220703, 2220700, 2220696],
 [2220690, 2220688, 2220687, 2220682, 2220676],
 [2220674, 2220671]]
```
* python 3, wo what the heck
```python
get_partitions(ids, 5)
# => 
[[2220706, 2220705, 2220703, 2220700, 2220696], 
[2220690, 2220688, 2220687, 2220682, 2220676], 
[2220674, 2220671], 
[2220700, 2220696, 2220690, 2220688, 2220687, 2220682, 2220676, 2220674, 2220671]]
```
* The file where this function exists was missing the standard `from __future__ import division, absolute_import, print_function, unicode_literals` line, so that's why this happened in the first place.
* The fix to make this work for both `python2` and `python3` was to rewrite the `/` division as `//` explicitly ...
```python
def get_partitions(vec, slice_size):
    assert slice_size > 0
    assert isinstance(vec, list)
    num_slices = len(vec)//slice_size
    size_remainder = len(vec) - num_slices*slice_size
    slices = [vec[k*slice_size:k*slice_size+slice_size] for k in range(num_slices)]

    if size_remainder:
        slices.append(vec[-(size_remainder):])

    return slices
```


#### No more Func name
- per https://docs.python.org/3/whatsnew/3.0.html#operators-and-special-methods godamn
- ` getattr(some_func, 'func_name')` to retrieve the name of a func. no longer works
- in python 3 that is now `some_func.__name__` :grimacing:
- similarly `some_func.func_code` was renamed to `some_func.__code__`

#### Notes on reading python2 pickle in python3
* Given a pandas DataFrame written like this, 
```python
cPickle.dumps(df)
```
* I was able to read it in python 3 like this
```python
with open('blah.pkl', 'rb') as fd:
    df = pickle.load(fd, encoding='latin1') 
    
# And if having read it from s3 to a bytes object, this worked too
df = pickle.loads(pkl, encoding='latin1')
```

#### Noticing boto3 uses `bytes` now now `str`
* Before it was possible to do this
```python
import boto3
import json
from StringIO import StringIO

client = boto3.client('lambda')
json_payload = json.dumps(payload)
s = StringIO(json_payload)

version = '4'
response = client.invoke(
        FunctionName='myBlahBlahLambda',
        InvocationType='RequestResponse', 
        LogType='Tail',
        Payload=s,
        Qualifier=version)
out_dict = json.loads(response.get('Payload').read())
return out_dict

```
* Now that complains with 
```
TypeError: Unicode-objects must be encoded before hashing
```
* But it works to use this instead...
```python
import boto3
import json
from io import BytesIO

client = boto3.client('lambda')
json_payload = json.dumps(payload).encode('utf-8') # <-- encode
s = BytesIO(json_payload)

version = '4'
response = client.invoke(
        FunctionName='myBlahBlahLambda',
        InvocationType='RequestResponse', 
        LogType='Tail',
        Payload=s,
        Qualifier=version)
out_dict = json.loads(response.get('Payload').read())
return out_dict
```


#### Bytes and json
* Relevant to data obtained with `requests` and `base64.b64encode` for example. These now produce `bytes` as opposed to `str`. 
```python 
          "TypeError: Object of type bytes is not JSON serializable",
```
* this comes up when trying to use `json.dumps` . Previously strings now `bytes` in there, 
* so typically need to b'blah'.decode('utf-8')

#### lambda cannot return bytes/json
```python
[ERROR] Runtime.MarshalError: Unable to marshal response: b'gAN9cQAoWA4
```
* happening when have bytes in the response..


#### Dict merging
* interestingly the `dict()` vs `{}` behavior is different..
```python
In [34]: dict(**{'hi': 'there'}, **{'hello': 'there'}, **{'hello': 'sailor'})           
---------------------------------------------------------------------------
TypeError                                 Traceback (most recent call last)
<ipython-input-34-3bc078749ddb> in <module>
----> 1 dict(**{'hi': 'there'}, **{'hello': 'there'}, **{'hello': 'sailor'})

TypeError: type object got multiple values for keyword argument 'hello'


In [36]: dict(list({'hi': 'there'}.items())+ list({'hello': 'there'}.items())+ list({'he
    ...: llo': 'sailor'}.items()))                                                      
Out[36]: {'hi': 'there', 'hello': 'sailor'}

In [37]: {**{'hi': 'there'}, **{'hello': 'there'}, **{'hello': 'sailor'}}               
Out[37]: {'hi': 'there', 'hello': 'sailor'}

```

#### urlparse
* From `import urlparse` 
* To `from urllib.parse import urlparse`
