#### histogram overlays

```python
# Nice technique from https://srome.github.io/Covariate-Shift,-i.e.-Why-Prediction-Quality-Can-Degrade-In-Production-and-How-To-Fix-It/ 
# ... put two histograms on same plot ...

def produce_overlayed_hists_for_col_dfs(col, dfs):
    fig = plt.figure(figsize=(12,12))

    ax = fig.add_subplot(121)
    ax.hist(dfs[0][1][col], color='r', alpha=0.2, bins=50)
    ax.hist(dfs[1][1][col], color='b', alpha=0.2, bins=50)
    ax.set(title=f'{dfs[0][0]} (red) vs {dfs[1][0]} (blue)',
            ylabel=col)
```

#### sparse diagonal x axis ticks

```python
import matplotlib.pyplot as plt
import pandas as pd
import datetime

def make_xtick_labels(x, step=5):
    '''Given x, step the labels every <step>
    Aka, take every <step>th x label
    '''
    x_ticks = [i for i in  range(len(x)) if i % step == 0]
    x_labels = [x[i] for i in x_ticks]
    return x_ticks, x_labels

```
* Did not add an example `x` , `y` yet, but showing an example where `x` contains dates and `y` is numeric.
```python
x = ?
y = ?

fig = plt.figure(figsize=(12,4))
ax = fig.add_subplot(111)
ax.plot(y)
x_ticks, x_labels = make_xtick_labels(x, step=20)
ax.set_xticks(x_ticks)
ax.set_xticklabels(x_labels, rotation=-45)
fig.show()


```

<img src="https://github.com/seanickle/handy/blob/master/assets/Screen%20Shot%202020-07-23%20at%2011.40.26%20AM.png?raw=true" width="50%">


#### Heatmaps are nice

```python
plt.figure(figsize=(10,10))
plt.imshow(bitmap)
plt.colorbar()
plt.grid(False)
plt.show()
```


#### using `np.histogram` and quantiles to spot check bimodal distributions
- I had this use case where I wanted to collect walltime from a service, from a dataset where a bimodal distribution was basically a given. I wanted thea mean of the second distribution. 
- Instead of trying to use clustering analysis like [dbscan](https://scikit-learn.org/stable/modules/generated/sklearn.cluster.DBSCAN.html) which would have probably worked, I just started collecting the time series `np.histogram` and quantile data and I was able to visually inspect / prove that the median is a good enough statistic in this case, without too much extra data preprocessing required!
- sampling data from athena every 7 days , here are two examples below. 

<img src="https://github.com/seanickle/handy/blob/master/assets/2020-07-25-handy-histograms/2020-07-24-zibby-timeline_30_0.png?raw=true">

<img src="https://github.com/seanickle/handy/blob/master/assets/2020-07-25-handy-histograms/2020-07-24-zibby-timeline_33_0.png?raw=true">

- supporting codes... (I didnt add code for `daa.run_it` but basically that just pulls data into a dataframe with a column  `backend_processing_time` that is being used here. And `make_query` just makes a query for a particular date to pull that data. So nothing really special about those. They can be replaced with any particular method of gathering data.)

```python
import datetime
from tqdm import tqdm
d1 = datetime.date(2019, 1, 1)
d2 = datetime.date(2020, 7, 1)
dd = ddu.range_dates(d1, d2, 7)

# outvec = []
for dt in tqdm(dd):
    query = make_query(dt)
    athenadf = daa.run_it(query, query_name='Unsaved')

    hist = np.histogram(athenadf.backend_processing_time.tolist(), 
                    bins=10, range=None)
    mean = np.mean(athenadf.backend_processing_time.tolist())
    quantiles = get_quantiles(athenadf.backend_processing_time.tolist())
    outvec.append({'hist': hist, 'quantiles': quantiles,
                  'date': dt.strftime('%Y-%m-%d'),
                  'mean': mean})
```

```python
import numpy as np
import matplotlib.pyplot as plt
def get_quantiles(unsorted):
    data = sorted(unsorted)
    minimum = data[0]
    Q1 = np.percentile(data, 25, interpolation = 'midpoint') 
    median = np.median(data)
    Q3 = np.percentile(data, 75, interpolation = 'midpoint') 
    maximum = data[-1]
    return [minimum, Q1, median, Q3, maximum]

def show_da_stats(bundle):
    H, bins = bundle['hist']
    quantiles = bundle['quantiles']
    # plt.plot(x[1][:-1], x[0], drawstyle='steps')
    #print(H, bins)
    #print(quantiles)
    plt.scatter(quantiles, [1, 1, 1, 1, 1])
    plt.axvline(quantiles[1], label='q:25%')
    plt.axvline(quantiles[2], label='q:50%')
    plt.axvline(quantiles[3], label='q:75%')
    
    plt.title(f"preapprove walltime histogram at {bundle['date']}")
    plt.plot(bins, np.insert(H, 0, H[0]), drawstyle='steps', color='green')
    plt.grid(True)
    plt.legend()
    plt.show()

bundle = outvec[0]
show_da_stats(bundle)
```



