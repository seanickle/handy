

#### Create a new user
```python
username = 'new_user_foo'
passw = input()
sql = "CREATE USER {} WITH PASSWORD '{}' ".format(username, passw)

```



#### Make some quick grants

```python
tables = ['table1',
'table_foo',
]
username = 'xx'
grant_queries = [q.format(username) for q in 
    ["GRANT CONNECT ON DATABASE mydb TO {}",
    "GRANT USAGE ON SCHEMA public TO {}",] + ["GRANT SELECT ON {} TO ".format(t) + " {} " for t in tables]]


```

#### check exissting users

```sql
select * from pg_user
```


#### update user password ; change
```
ALTER USER user_name WITH PASSWORD 'new_password';
```
* can use `input()` here too actually 

#### Check Existing Grants / permissions
* The user running this query might not be able to see all the rows
```sql
SELECT table_catalog, table_schema, table_name, privilege_type, grantee
FROM   information_schema.table_privileges 
where grantee = 'foo_user'

```


#### Check what roles `blah_user` is a part of 

```sql
WITH RECURSIVE cte AS (
   SELECT oid FROM pg_catalog.pg_roles WHERE rolname = 'blah_user'

   UNION ALL
   SELECT m.roleid
   FROM   cte
   JOIN   pg_catalog.pg_auth_members m ON m.member = cte.oid
   )
SELECT oid, oid::regrole::text AS rolename FROM cte;  -- oid & name

```

#### Find owner of table

```sql
--
select tablename, tableowner from pg_catalog.pg_tables 
where schemaname = 'fooschema' 
and tablename = 'footable'

```

tablename|tableowner
--|--
footable|foouser

