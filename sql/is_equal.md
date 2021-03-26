Good to know.
```python

sql = '''
select id,
true is true true_is_true,
true = true true_eq_true,
false is false false_is_false,
false = false false_eq_false,
true is false true_is_false,
true = false true_eq_false,
false is true false_is_true,
false = true false_eq_true,
null  = true null_eq_true,
null is true null_is_true,
null = false null_eq_false,
null is false null_is_false,
true is null true_is_null,
true = null true_eq_null,
false is null false_is_null,
false = null false_eq_null,
null is null null_is_null,
null = null null_eq_null
from blahblah limit 1
'''
db.read(sql, stage='foo')


Out[125]: 
[{'id': 4270416,
  'true_is_true': True,
  'true_eq_true': True,
  'false_is_false': True,
  'false_eq_false': True,
  'true_is_false': False,
  'true_eq_false': False,
  'false_is_true': False,
  'false_eq_true': False,
  'null_eq_true': None,
  'null_is_true': False,
  'null_eq_false': None,
  'null_is_false': False,
  'true_is_null': False,
  'true_eq_null': None,
  'false_is_null': False,
  'false_eq_null': None,
  'null_is_null': True,
  'null_eq_null': None}]
```
