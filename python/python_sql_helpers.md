
## Generate a CTE from a local csv file

```python
import pandas as pd

replace_nan = lambda x: x.replace('nan', 'null')


def df_to_values(df, columns=None, replace_nans=True):
    if columns is None:
        columns = df.columns.tolist()

    newdata = str(list(df[columns].to_records(
        index=False))
    )[1:-1]
    if replace_nans:
        newdata = replace_nan(newdata)

    return newdata

  
def cte_from_csv(localfile, colgroups, cte_names, head=False):
    df = pd.read_csv(localfile)
    if head:
        df = df.head()
    return 'with ' + ', '.join([
        f'''
        {cte_names[i]}({', '.join(colgroups[i])}) as (
            VALUES {df_to_values(df, columns=colgroups[i], replace_nans=True)}
        )
    '''
        for i, _ in enumerate(colgroups)
    ])
```

#### temp.csv
```
one,two,three
1,2.,3.3
,2.3,3.5
11,.22,.003
```

#### Example


```python
loc = "temp.csv"
print( cte_from_csv(loc, [['one', 'two', 'three']], ['foo'],))
```
* => 
```sql
with 
        foo(one, two, three) as (
            VALUES (1., 2., 3.3), (null, 2.3, 3.5), (11., 0.22, 0.003)
        )
    
```

## Dollar encode 

```python
def doll_df_to_values(df, cols,
            cols_to_dollar_encode=None):

    data = [tuple(x) for x in df[cols].to_records(index=False)]
    vec = ['(' + ', '.join([enc(x[i], cols[i] in cols_to_dollar_encode)  
        for (i, _) in enumerate(x)]) + ')' for x in data]

    return ', '.join(vec)


def enc(x, dollar_enc):
    if dollar_enc:
        return f'$${x}$$'
    elif isinstance(x, str):
        return f"'{x}'"
    elif np.isnan(x):
        return 'null'
    else:
        return f"{x}"

```
