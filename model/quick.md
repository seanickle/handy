


```python

from sklearn.datasets import load_iris
from sklearn.linear_model import LogisticRegression
X, y = load_iris(return_X_y=True)
clf = LogisticRegression(
	random_state=0,
	penalty="l2",
	).fit(X, y)
clf.predict(X[:2, :])

clf.predict_proba(X[:2, :])


clf.score(X, y)

clf.decision_function(X)

```

```python
from sklearn import metrics
from sklearn.ensemble import RandomForestClassifier
from sklearn.datasets import make_classification
X, y = make_classification(n_samples=1000, n_features=4,
                           n_informative=2, n_redundant=0,
                           random_state=0, shuffle=False)
clf = RandomForestClassifier(
	max_depth=2,
	random_state=0,
	n_estimators=100,
	class_weight= # "balanced", "balanced_subsample" or {0: 0.1, 1: 0.9 } weights per class 
)
clf.fit(X, y)

print(clf.predict([[0, 0, 0, 0]]))


In [16]: pd.DataFrame(X).corr()                                                          
Out[16]: 
          0         1         2         3
0  1.000000  0.065124  0.026765  0.028988
1  0.065124  1.000000  0.031176 -0.026317
2  0.026765  0.031176  1.000000 -0.006788
3  0.028988 -0.026317 -0.006788  1.000000

In [17]: clf.feature_importances_                                                        
Out[17]: array([0.14205973, 0.76664038, 0.0282433 , 0.06305659])



print(clf.predict_log_proba([[0, 0, 0, 0]]))

print(clf.predict_proba([[0, 0, 0, 0]]))

print(clf.predict([[0, 0, 0, 0]]))


In [18]: print(clf.predict_log_proba([[0, 0, 0, 0]])) 
    ...:                                                                                 
[[-1.72562562 -0.19608985]]

In [19]: print(clf.predict_proba([[0, 0, 0, 0]])) 
    ...:                                                                                 
[[0.17806162 0.82193838]]

In [20]: from math import log                                                            

In [21]: log(0.82193838)                                                                 
Out[21]: -0.19608985023951067

In [22]: print(clf.predict([[0, 0, 0, 0]]))                                              
[1]


y_true = y
y_pred = clf.pred(X)
metrics.accuracy_score(y_true, y_pred)
Out[29]: 0.925


metrics.confusion_matrix(y_true, y_pred)
Out[30]: 
array([[434,  70],
       [  5, 491]])


fpr, tpr, thresholds = metrics.roc_curve(y_true, y_pred, pos_label=1)
metrics.auc(fpr, tpr)
# Out[32]: 0.9255152329749103

```

```
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler
```


