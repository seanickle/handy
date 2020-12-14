This may apply to Athena and or prestodb in general


#### array empty? 
* Per [stackoverflow](https://stackoverflow.com/questions/44192105/checking-if-a-map-or-array-is-empty-in-presto) learned the name for this in [the docs](https://prestodb.io/docs/current/functions/array.html#cardinality) is `cardinality`
```sql
select cardinality(array[]) = 0;
```
