
### Date partition
* One week at a time,
* _Note, I see the very last date below, is not included some might be one small bug here._ 
```python
start_str, end_str = ("2021-08-06", "2021-10-19")

dates = range_dates(date_from_date_str(start_str), date_from_date_str(end_str), step=7)
slices = get_uniform_slices(dates, 14)
slices
```
```
[[datetime.date(2021, 8, 6), datetime.date(2021, 8, 19)],
 [datetime.date(2021, 8, 20), datetime.date(2021, 9, 2)],
 [datetime.date(2021, 9, 3), datetime.date(2021, 9, 16)],
 [datetime.date(2021, 9, 17), datetime.date(2021, 9, 30)],
 [datetime.date(2021, 10, 1), datetime.date(2021, 10, 14)],
 [datetime.date(2021, 10, 15), datetime.date(2021, 10, 18)]]
```

```python
import datetime

def date_from_date_str(date_str):
    return datetime.datetime.strptime(date_str, "%Y-%m-%d").date()

def range_dates(start, end, step=1):
    assert start <= end

    dates = []
    next_date = start
    while next_date < end:
        dates.append(next_date)
        next_date += datetime.timedelta(days=step)

    if dates and dates[-1] < end - datetime.timedelta(days=1):
        dates.append(end - datetime.timedelta(days=1))
    return dates

def get_partitions(vec, slice_size):
    assert slice_size > 0
    assert isinstance(vec, list)
    num_slices = len(vec) // slice_size
    size_remainder = len(vec) - num_slices * slice_size
    slices = [vec[k * slice_size : k * slice_size + slice_size] for k in range(num_slices)]

    if size_remainder:
        slices.append(vec[-(size_remainder):])

    return slices

def get_uniform_slices(vec, slice_size):
    parts = get_partitions(vec, slice_size=slice_size)
    return [[part[0], part[-1]] for part in parts]

```
