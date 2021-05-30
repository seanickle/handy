
Glom does not let you write or at least I couldnt figure out how. Tinkering with a spec based writer... 

```python
from glom import glom, PathAccessError

def nested_assign(target, spec, value):
    parts = spec.split(".")
    last = parts[-1]

    while parts[:-1]:
        top = parts.pop(0)
        target = target[top]

    target[last] = value



def _plant(target, spec):
    """This is the equivalent of mkdir -p blah/flarg/blarg/klarf
    """
    parts = spec.split(".")
    try:
        for i, part in enumerate(parts):
            glom(target, ".".join(parts[:i + 1]))
    except PathAccessError as e:
        print(repr(e))
        print("stopped at ", i, part)
        print("going to add remaining", parts[i:])
        print("..", list(range(i, len(parts))))

        for j in range(i + 1, len(parts)):
            this_spec = ".".join(parts[:j])
            print("this_spec", this_spec)
            nested_assign(target, this_spec, {})

            
def transplant(source_dict, mappings):
    skeleton = {}

    for m in mappings:
        _plant(skeleton, m["original"])
        value = glom(source_dict, m["new"])
        nested_assign(skeleton, m["original"], value)

    return skeleton


```

#### Note
* I saw [someone on stack overflow](https://stackoverflow.com/questions/17980509/python-recursive-setattr-like-function-for-working-with-nested-dictionaries) had a similar question but the approach looks a bit complex. May look at this later.
* 
