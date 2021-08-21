
#### subtract intervals from dates
although in postgresql you can freely add/subtract dates/timestamps and intervals, 
```sql
SELECT '2001-01-01'::timestamp + '1 year'::interval;
```
in mysql land you need to do use `date_sub` and `date_add`
```sql
date_sub('2019-06-30' , interval 90 days)
```

#### Subtract dates
Also in postgresql you can just subtract dates, 
```sql
'2021-01-01'::date -  '2021-05-01'::date
```
And in mysql to do this you can do 
```sql
DATEDIFF('2021-01-01', '2021-05-01')
```
