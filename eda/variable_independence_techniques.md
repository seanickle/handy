

Appreciate [this post](https://stackabuse.com/statistical-hypothesis-analysis-in-python-with-anovas-chi-square-and-pearson-correlation/) on helping to choose between a few available tests in determining if there are meaningful relationships between feature data. In particular,
- ANOVA compares two variables, where one is categorical (binning is helpful here) and one is continuous.
- Chi-square is useful for two categorical comparing two cateorical varables, on the other hand.
- And  Pearson Correlation can be used between two continiuous variables 
    - But the caveat is that this test assumes both variables are normally distributed 
    - And outliers should be chopped off with some preprocessing.
