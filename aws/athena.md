This may apply to Athena and or prestodb in general


#### array empty? 
* Per [stackoverflow](https://stackoverflow.com/questions/44192105/checking-if-a-map-or-array-is-empty-in-presto) learned the name for this in [the docs](https://prestodb.io/docs/current/functions/array.html#cardinality) is `cardinality`
```sql
select cardinality(array[]) = 0;
```
* This cannot be applied to the output of a `json_extract(json_parse(data), '$.blah.flah.clah')`  since `cardinality()` takes `ARRAY` and not `JSON`.
* However, that `JSON` can be cast. For example, if `'$.blah.flah.clah'` is like `[{"hi": "there"}, {"so": "then"}]`, then this 

```sql
cardinality(cast(json_extract(json_parse(what), '$.blah.flah.clah') as array(map(varchar, varchar))))
```

will produce the length of those arrays.
