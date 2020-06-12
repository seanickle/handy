```
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
