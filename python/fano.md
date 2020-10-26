

```python
import numpy as np

from bokeh.plotting import figure, show, output_file
def doplot(x, y, **figure_kwargs):
    N = x.shape[0]
    radii = np.array([0.1,]*N)
    # print 'DEBUG, ', radii[:4], ', ', N
    colors = [
        "#%02x%02x%02x" % (int(r), 
        int(g), 150) for r, g in zip(50+2*x, 30+2*y)
    ]

    TOOLS="hover,crosshair,pan,wheel_zoom,zoom_in,zoom_out,box_zoom,undo,redo,reset,tap,save,box_select,poly_select,lasso_select,"
    p = figure(tools=TOOLS, **figure_kwargs)

    p.scatter(x, y, radius=radii,
          fill_color=colors, fill_alpha=0.6,
          line_color=None)

    output_file("color_scatter.html")

    show(p)  # open a browser

def make_data(N=100, trials=1000, minmax=(0, 1)):
    a, b = minmax
    data = [[sum(vec), fano(vec)]
    for vec in [a + (b - a)*np.random.random_sample(N)
        for i in range(trials)]]
    vec1, vec2 = zip(*data)
    return np.array(vec1), np.array(vec2)

```

```python
figure_kwargs = {'x_axis_label': 'sum(X)', 'y_axis_label': 'fano(X)', 'title': 'sum(X) vs fano(X)'}
doplot(*make_data(minmax=(0,1)), **figure_kwargs)
```

![image](https://user-images.githubusercontent.com/2048242/50387562-46f47900-06cd-11e9-8e60-25481624b99c.png)
