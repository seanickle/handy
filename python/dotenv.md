#### environmental variable local injection
* using https://pypi.org/project/python-dotenv/ 

```
pip install -U python-dotenv
```
Given a file like `.env.test` ...

```
FOO=hi
```

```python
from dotenv import load_dotenv, find_dotenv

load_dotenv(find_dotenv(".env.test", raise_error_if_not_found=True))
import os
os.getenv('FOO') # => 'hi'
```
