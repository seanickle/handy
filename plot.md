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

