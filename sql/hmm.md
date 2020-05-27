#### List indexes
* From [here](https://www.postgresqltutorial.com/postgresql-indexes/postgresql-list-indexes/)
```sql
SELECT
    tablename,
    indexname,
    indexdef
FROM
    pg_indexes
WHERE
    schemaname = 'public'
    
ORDER BY
    tablename,
    indexname;

```

#### Disk Usage per table

* from [the postgresql wiki](https://wiki.postgresql.org/wiki/Disk_Usage)
* except one minor change ... for `('user_blah', 'user_blar', 'schema1', 'schema2')` schemas only ... 
```sql
SELECT *, pg_size_pretty(total_bytes) AS total
    , pg_size_pretty(index_bytes) AS INDEX
    , pg_size_pretty(toast_bytes) AS toast
    , pg_size_pretty(table_bytes) AS TABLE
  FROM (
  SELECT *, total_bytes-index_bytes-COALESCE(toast_bytes,0) AS table_bytes FROM (
      SELECT c.oid,nspname AS table_schema, relname AS TABLE_NAME
              , c.reltuples AS row_estimate
              , pg_total_relation_size(c.oid) AS total_bytes
              , pg_indexes_size(c.oid) AS index_bytes
              , pg_total_relation_size(reltoastrelid) AS toast_bytes
          FROM pg_class c
          LEFT JOIN pg_namespace n ON n.oid = c.relnamespace
          WHERE relkind = 'r'
          and nspname in ('user_blah', 'user_blar', 'schema1', 'schema2')
  ) a
) a
```



#### detect blocked queries?

* This didnt exactly work for me as expected, but colleague had mentioned this ... 
```sql
SELECT
  COALESCE(blockingl.relation::regclass::text,blockingl.locktype) as locked_item,
  now() - blockeda.query_start AS waiting_duration, blockeda.pid AS blocked_pid,
  blockeda.query as blocked_query, blockedl.mode as blocked_mode,
  blockinga.pid AS blocking_pid, blockinga.query as blocking_query,
  blockingl.mode as blocking_mode
FROM pg_catalog.pg_locks blockedl
JOIN pg_stat_activity blockeda ON blockedl.pid = blockeda.pid
JOIN pg_catalog.pg_locks blockingl ON(
  ( (blockingl.transactionid=blockedl.transactionid) OR
  (blockingl.relation=blockedl.relation AND blockingl.locktype=blockedl.locktype)
  ) AND blockedl.pid != blockingl.pid)
JOIN pg_stat_activity blockinga ON blockingl.pid = blockinga.pid
  AND blockinga.datid = blockeda.datid
WHERE NOT blockedl.granted
AND blockinga.datname = current_database()


```



#### hmmmm how about this

```sql
select blockingl.relation, blockingl.pid, blockingl.mode, blockingl.granted,
        pgclass.relname, stat.usename, stat.application_name, stat.wait_event_type, stat.wait_event, stat.state, stat.query
    from pg_catalog.pg_locks blockingl
    join pg_class pgclass on blockingl.relation = pgclass.oid 
    join pg_stat_activity stat on stat.pid = blockingl.pid 

```
