
#### Search and return json paths
```python
def substring_exists_lower(substring, string):
    # f = lambda key, term: term in key.lower()
    return substring.lower() in string.lower()

def path_join(path, key):
    return f'{path}{"." if path else ""}{key}'

def find_term(path, term, node, found, only_leaves=False):
    # must be dict or list
    if not ((isinstance(node, dict)) or (isinstance(node, list))):
        return

    # look in this node
    if isinstance(node, dict):
        for key in node.keys():
            if substring_exists_lower(term, key):
                if only_leaves:
                    if not ((isinstance(node[key], dict)) or (isinstance(node[key], list))):
                        found.add(path_join(path, key))
                else:
                    found.add(path_join(path, key))

        for key in node.keys():
            #if isinstance(node[key], dict):
            find_term(path_join(path, key), term, node[key], found, only_leaves)

    if isinstance(node, list):
        for i, x in enumerate(node):
            find_term(f'{path}[{i}]', term, node[i], found, only_leaves)

```

#### Example
```python

found = set()
find_term('', 'name', {}, found)
```
