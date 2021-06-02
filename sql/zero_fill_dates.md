
### `lpad` is the func to make sure a month is always two digits as an example.

```sql

select 
concat( extract (year from  foo.timestamp)::text, 
lpad (extract (month from  foo.timestamp)::text, 2, '0') ) 

 as yearmonth, 
count(1) 
from foo
where 
group by yearmonth 
order by yearmonth  asc 

```

yearmonth|count
--|--
202005   | 5208
202006   | 8584
202007   | 7780
202008   | 5382
202009   | 3635
202010   | 2791
202011   | 1284
202012   | 2704
202101   | 2416
202102   | 1964
202103   | 2554
202104   | 2935
202105   | 2909
202106   |  160
