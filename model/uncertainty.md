

#### mean/std technique For xgboost/random forest type models
*  per [this article](https://towardsdatascience.com/harnessing-the-power-of-uncertainty-3ad9431e595c) , the proposed idea is to use the preditions of all the trees as the prediction space or a kind of an uncertainty interval.
* I wonder if we can say predictions that a model is more certain about have a tighter distribution of predictions. And conversely that a model is unsure about its predictions if the distribution of predictions is wide.
* I have a feeling that the LSS approach to XGBoost [here](https://www.groundai.com/project/xgboostlss-an-extension-of-xgboost-to-probabilistic-forecasting/2) tries to automate something like that. 

### References hmm
* [mean/std technique](https://towardsdatascience.com/harnessing-the-power-of-uncertainty-3ad9431e595c)
* [intervals](https://towardsdatascience.com/regression-prediction-intervals-with-xgboost-428e0a018b)

