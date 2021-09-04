

#### F test statistic to evaluate the features
* One F test produces a ratio (called an F-value) comparing the variation between two populations' sample means and the variation _within_ the samples. With a greater variation between the population samples, we are more likely to reject the null hypothesis that the samples are of the same source distribution. With a higher F-value, the lower the p-value associated for the distribution of this test.  [1] . 
* Also good example at the [2]
* Here below, I had a _DataFrame_ , `df` with some features, `f1, f2, f3, f4` and target , `y` , 
* Based on my results, `f3` is great, `f4` barely better than random numbers.
* `f_regression` is meant for real number `y` 
* And `f_classif` should only be used for a classification problem where `y` is a class. [3]

```python
from sklearn.feature_selection import f_regression, mutual_info_regression

def evaluate_feature(df, feature, target):
    X = np.array(df[feature].tolist())
    num_rows = X.shape[0]
    X = np.reshape(X, (num_rows, 1))
    y = df[target].tolist()
    f_value, _ = f_regression(X, y)
    print(feature, f_value)

num_rows = df.shape[0]

# Random
X = np.random.rand(num_rows, 1)
f_value, _ = f_regression(X, y)
print('random, ', f_value)

# Random
X = np.random.rand(num_rows, 1)
f_value, _ = f_regression(X, y)
print('random, ', f_value)

# Random
X = np.random.rand(num_rows, 1)
f_value, _ = f_regression(X, y)
print('random, ', f_value)

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

### Refs
[1] https://www.statology.org/what-does-a-high-f-value-mean/
[2] https://scikit-learn.org/stable/auto_examples/feature_selection/plot_f_test_vs_mi.html
[3] https://scikit-learn.org/stable/modules/generated/sklearn.feature_selection.f_classif.html#sklearn.feature_selection.f_classif
