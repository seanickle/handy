
#### Search and return json paths
```python
def path_join(path, key):
    return f'{path}{"." if path else ""}{key}'

def find_term(path, term, node, found, f, only_leaves=False):
    # must be dict or list
    if not ((isinstance(node, dict)) or (isinstance(node, list))):
        return

    # look in this node
    if isinstance(node, dict):
        for key in node.keys():
            if f(key, term):
                if only_leaves:
                    if not ((isinstance(node[key], dict)) or (isinstance(node[key], list))):
                        found.add(path_join(path, key))
                else:
                    found.add(path_join(path, key))

        for key in node.keys():
            #if isinstance(node[key], dict):
            find_term(path_join(path, key), term, node[key], found, f, only_leaves)

    if isinstance(node, list):
        for i, x in enumerate(node):
            find_term(f'{path}[{i}]', term, node[i], found, f, only_leaves)

```

#### Example
```python
f = lambda key, term: term in key
found = set()
find_term('', 'name', {}, found, f)
```
