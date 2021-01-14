
#### Initial stab on interpreting Variance inflation factor (VIF)
So far my skim on https://en.wikipedia.org/wiki/Variance_inflation_factor and https://en.wikipedia.org/wiki/Multicollinearity   tells me that high Variance Inflation Factor (VIF) indicates high multicolinearity w/ one or more other independent variables. And that’s bad because
- (a) when building a linear model (at least using ordinary least squares (OLS) , not yet sure if this is still true if you use regularization ) , the coefficients calculated for the independent variables can change “erratically” given slightly different data .
- (b) So (a) tells me that those coefficients maybe are less useful in using those coefficients to interpret importance for instance.
- (c) That smells like a risk of overfitting to me.
- (d) Sounds like  also building those coefficients is more deterministic for adjustments/data updates

- Therefore eliminating variables w/ high VIF reduces overfitting risk and improves overall interpretability / stability

- _(Oh yea and a cutoff of `5` to `10` is mentioned to be common )_

