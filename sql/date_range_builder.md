


```python
import datetime

def date_from_date_str(date_str):
    return datetime.datetime.strptime(date_str, '%Y-%m-%d').date()


def make_start_end_clauses(start, end):
    '''Make sql to take advantage of Athena date partitioning.
    Example
    WHERE ((year = 2017 AND month = 10 AND day >=30)
            OR (year = 2017 AND month = 11 AND day = 1))'''
    assert start <= end
    month_tuples = make_start_end_month_tuples(start, end)
    clauses = []
    if len(month_tuples) == 1:
        clause_raw = ('(year = {} AND month = {} AND day BETWEEN {} AND {})')
        clause = clause_raw.format(start.year, start.month, start.day, end.day)
        clauses.append(clause)
    else:
        # First month.
        start_clause = '(year = {} AND month = {} AND day >= {})'.format(
            start.year,
            start.month,
            start.day, )
        clauses.append(start_clause)

        # Middle months, if any.
        months_in_between = month_tuples[1:-1]
        for year, month in months_in_between:
            clauses.append(
                '(year = {} AND month = {})'.format(year, month))

        # Last month.
        end_clause = '(year = {} AND month = {} AND day <= {})'.format(
            end.year,
            end.month,
            end.day, )
        clauses.append(end_clause)

    all_clause = ' OR '.join(clauses)
    final_clause = ' ( {} ) '.format(all_clause)
    return final_clause


def make_date_clause(start_str=None, end_str=None, start_date=None, end_date=None):
    '''If given a start, return sql from start until today.
    Otherwise, return sql which only goes back 7 days.
    '''
    if start_str is None and end_str is None and start_date is None and end_date is None:
        # do 7 days...
        today = utc_today()
        date_clause = date_clause_from_days_ago(today, days_ago=7)
        return date_clause

    if start_str is not None and end_str is not None:
        start_date = date_from_date_str(start_str)
        end_date = date_from_date_str(end_str)
        date_clause = make_start_end_clauses(start=start_date, end=end_date)
        return date_clause

    if start_date is not None and end_date is not None:
        date_clause = make_start_end_clauses(start=start_date, end=end_date)
        return date_clause


def make_start_end_month_tuples(start, end):
    assert start <= end
    tuples = set([(start.year, start.month)])
    next_date = start
    while next_date <= end:
        tuples |= set([(next_date.year, next_date.month)])
        next_date += datetime.timedelta(days=1)
    return sorted(list(tuples))

```

#### Examples

```python
import datetime

make_start_end_clauses(datetime.date(2021,4,29), datetime.date(2021, 5, 5))
# ' ( (year = 2021 AND month = 4 AND day >= 29) OR (year = 2021 AND month = 5 AND day <= 5) ) '

make_start_end_clauses(datetime.date(2021,4,29), datetime.date(2022, 5, 5))
# ' ( (year = 2021 AND month = 4 AND day >= 29) OR (year = 2021 AND month = 5) OR (year = 2021 AND month = 6) OR (year = 2021 AND month = 7) OR (year = 2021 AND month = 8) OR (year = 2021 AND month = 9) OR (year = 2021 AND month = 10) OR (year = 2021 AND month = 11) OR (year = 2021 AND month = 12) OR (year = 2022 AND month = 1) OR (year = 2022 AND month = 2) OR (year = 2022 AND month = 3) OR (year = 2022 AND month = 4) OR (year = 2022 AND month = 5 AND day <= 5) ) '



```
