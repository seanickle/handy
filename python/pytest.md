
#### Adding options to your tests
If you add a `conftest.py` to your `tests` dir, 

```python
import os


def pytest_addoption(parser):
    parser.addoption("--cool-option", action="store", default="foo")
    parser.addoption("--some-dir", action="store", default="blah")

```

And in your `test_foo.py` you can then do 

```python
import pytest

@pytest.fixture()
def out_dir(pytestconfig):
    return pytestconfig.getoption("out_dir")


def test_some_test(out_dir, cool_option):
    print("I will use this ", out_dir)
    ...
    

def test_other_test(out_dir):
    print("I will use this ", out_dir)
    ...
    
    
```


* And run like 

```
pytest test_foo.py::test_some_test --out-dir "some/dir/I/Want" --cool-option "hi"
pytest test_foo.py::test_other_test --out-dir "some/dir/I/Want"
```
