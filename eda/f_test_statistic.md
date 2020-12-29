

#### F test statistic to evaluate the features
* Also good example at the [source](https://scikit-learn.org/stable/auto_examples/feature_selection/plot_f_test_vs_mi.html)
* Here below, I had a _DataFrame_ , `df` with some features, `f1, f2, f3, f4` and target , `y` , 
* Based on my results, `f3` is great, `f4` barely better than random numbers.

```python
from sklearn.feature_selection import f_regression, mutual_info_regression

def evaluate_feature(df, feature, target):
    X = np.array(df[feature].tolist())
    num_rows = X.shape[0]
    X = np.reshape(X, (num_rows, 1))
    y = df[target].tolist()
    f_test, _ = f_regression(X, y)
    print(feature, f_test)

num_rows = df.shape[0]

# Random
X = np.random.rand(num_rows, 1)
f_test, _ = f_regression(X, y)
print('random, ', f_test)

# Random
X = np.random.rand(num_rows, 1)
f_test, _ = f_regression(X, y)
print('random, ', f_test)

# Random
X = np.random.rand(num_rows, 1)
f_test, _ = f_regression(X, y)
print('random, ', f_test)

for feature in ['y',
                'f1',
                'f2',
                'f3',
                'f4',
                ]:
    evaluate_feature(df, feature, 'y')
```
```python
random,  [0.42851302]
random,  [0.60725371]
random,  [0.56094036]
y [3.50677485e+16]
f1 [52.90786486]
f2 [900.76441029]
f3 [4145.1618757]
f4 [1.22335227]
```


