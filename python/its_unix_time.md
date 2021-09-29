

```python
import datetime
import calendar
import pytz
import time


def unix_to_dt(unix):
    # unix timestamps can be 10 or 13 digits (until the year 2286 that is!)
    if len(str(unix)) == 13:
        unix /= 1000
    return datetime.datetime.fromtimestamp(unix, tz=pytz.UTC)


def bad_dt_to_unix_ts(dt):
    return f"{int(time.mktime(dt.timetuple()))}.{dt.microsecond}"


def dt_to_unix_ts_utc(dt):
    """Fork of unix_ts only for UTC datetime"""
    assert dt.tzinfo == pytz.UTC
    return calendar.timegm(dt.utctimetuple())

```
The `bad_dt_to_unix_ts` is a bit of a danger zone . look how the reversal is messed up...

```python
dt1 = datetime.datetime(2021, 9, 5, 0, 14, 1).replace(tzinfo=pytz.UTC)
print("dt1", dt1)

unix_ts = int(float(bad_dt_to_unix_ts(dt))) * 1000
print("unix_ts ", unix_ts)

# Reverse...
dt2 = unix_to_dt(unix_ts)
print("dt2", dt2)

dt1 2021-09-05 00:14:01+00:00
unix_ts  1630818841000
dt2 2021-09-05 05:14:01+00:00

```

#### Instead ..

```python
dt1 = datetime.datetime(2021, 9, 5, 0, 14, 1).replace(tzinfo=pytz.UTC)
print("dt1", dt1)

unix_ts = int(float(dt_to_unix_ts_utc(dt1))) * 1000
print("unix_ts ", unix_ts)

# Reverse...
dt2 = unix_to_dt(unix_ts)
print("dt2", dt2)


dt1 2021-09-05 00:14:01+00:00
unix_ts  1630800841000
dt2 2021-09-05 00:14:01+00:00
```
