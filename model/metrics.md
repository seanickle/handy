
###  TPR, FPR

```python
tpr = 1.0*TP/(FN + TP) # aka recall
fpr = 1.0*FP/(FP + TN) # 
```

### Confusion matrix
* Given a `testdf` where first column contains actual labels, `0`, `1`, and `predictions` is a list of probabilities, 
```python
y_pred = (y_prob >= 0.08)
confusion = pd.crosstab(index=y_true, 
            columns=y_pred, 
            rownames=['actual'], colnames=['predictions'])
```

predictions	|False	|True
--|--|--
actual		||
0|	509	|132
1|	32	|22



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


#### ks for a cutoff 

```python
def get_flargs(confusion):
    cols = confusion.columns.tolist()
    if False not in cols:
        TN = 0
        FN = 0
    else:
        TN = confusion.loc[0, False] # loc[0, 0] this works in newer pandas , not 0.18
        FN = confusion.loc[1, False]

    if True not in cols:
        FP = 0
        TP = 0
    else:
        FP = confusion.loc[0, True]
        TP = confusion.loc[1, True]
        
    return (TP, FP, TN, FN)
        
def calc_f1(TP, FP, TN, FN):
    if (FP + TP) == 0 or (FN + TP) == 0:
        return np.nan
    
    precision = 1.0*TP/(FP + TP)
    recall = 1.0*TP/(FN + TP)

    return {2*(precision*recall)/(precision + recall)}

def ks_for_cutoff(TP, FP, TN, FN):
    
    #  It is the maximum difference between TPR (aka recall) and FPR (aka fall-out)
    tpr = 1.0*TP/(FN + TP) #  = TP / P #  aka recall
    fpr = 1.0*FP/(FP + TN) #  = FP / N #  aka fall-out
    return tpr - fpr
    

def thisthings(y_true, y_prob):
    cutoffs = np.arange(0.01, 1.0, 0.01)
    f1_vec = []
    ks_vec = []
    tpr_vec = []
    fpr_vec = []
    tnr_vec = []

    for c in cutoffs:
        y_pred = (y_prob > c)
        confusion = pd.crosstab(index=y_true, 
                    columns=y_pred, 
                    rownames=['actual'], colnames=['predictions'])

        # print (c, confusion.shape, confusion.columns.tolist())

        (TP, FP, TN, FN) = get_flargs(confusion)
        try:
            tpr = 1.0*TP/(FN + TP) # aka recall
        except:
            tpr = np.nan

        try:
            fpr = 1.0*FP/(FP + TN)
        except:
            fpr = np.nan

        tpr_vec.append(tpr)
        fpr_vec.append(fpr)

        # f1 = calc_f1(confusion)
        f1 = sklearn.metrics.f1_score(y_true, y_pred)
        f1_vec.append(f1)

        ks = ks_for_cutoff(TP, FP, TN, FN)
        ks_vec.append(ks)

    return [cutoffs,
            f1_vec,
            ks_vec,
            tpr_vec,
            fpr_vec,
            tnr_vec]

```

```python
[cutoffs,
            f1_vec,
            ks_vec,
            tpr_vec,
            fpr_vec,
            tnr_vec] = thisthings(y_true, y_prob)
            
plt.plot(cutoffs[:20], np.array(ks_vec)[:20], label='ks')
plt.plot(cutoffs[:20], np.array(fpr_vec)[:20], label='fpr')
plt.plot(cutoffs[:20], np.array(tpr_vec)[:20], label='tpr')
plt.xlabel('cutoff')

plt.legend()
plt.show()

```

<img src="https://github.com/seanickle/handy/blob/master/assets/ks.png">


### References
* https://www.datavedas.com/model-evaluation-in-python/

