

### Confusion matrix
* Given a `testdf` where first column contains actual labels, `0`, `1`, and `predictions` is a list of probabilities, 
```python
pd.crosstab(index=testdf.iloc[:, 0], columns=np.round(predictions), rownames=['actual'], colnames=['predictions'])
```
predictions	|0.0|1.0
--|--|--
actual	| |
0	| 653 | 45 
1	| 51 | 12


### f1
```python
def calc_f1(confusion):
    TN = confusion.loc[0, 0]
    FP = confusion.loc[0, 1]
    FN = confusion.loc[1, 0]
    TP = confusion.loc[1, 1]
    
    precision = TP/(FP + TP)
    recall = TP/(FN + TP)
    return 2*(precision**2)/(precision + recall)
    
predictions = [] # list of probabilities , e.g. array([0.05567192, 0.03781519, 0.05437384, 0.01572161, ...])
cutoffs = np.arange(0.01, 0.5, 0.01)
f1_vec = []
for c in cutoffs:
    confusion = pd.crosstab(index=testdf.iloc[:, 0], 
                columns= (predictions > c), 
                rownames=['actual'], colnames=['predictions'])
    try:
        f1 = calc_f1(confusion)
    except TypeError:
        f1 = np.nan
    f1_vec.append(f1)
    

# fig = plt.figure()
plt.plot(cutoffs, np.array(f1_vec))
plt.xlabel('cutoff')
plt.ylabel('f1')
plt.show()
```

<img src="https://github.com/seanickle/handy/blob/master/assets/f1-output.png">
