

#### Make a zip file for a lambda layer
* From a well written reference [here](https://medium.com/swlh/how-to-create-and-use-layer-for-python-aws-lambda-function-80bc6eefa331) : 
* adjust the python version as needed
```
#!/bin/bash
export LIB_DIR="python"

rm -rf ${LIB_DIR} && mkdir -p ${LIB_DIR}

docker run --rm -v $(pwd):/foo -w /foo lambci/lambda:build-python3.8 \
    pip install -r requirements.txt -t ${LIB_DIR}

zip -r layer.zip python
```
* And I like to use `vim layer.zip` to look at the contents 
