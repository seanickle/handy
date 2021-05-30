

#### Make a zip file for a lambda layer
* From a well written reference [here](https://medium.com/swlh/how-to-create-and-use-layer-for-python-aws-lambda-function-80bc6eefa331) : 
* adjust the python version as needed

```bash
#!/bin/bash

echo -e "blah-lib==2.0\n\
umm-lib==0.45" > requirements.txt

export LIB_DIR="python"

rm -rf ${LIB_DIR} && mkdir -p ${LIB_DIR}

docker run --rm -v $(pwd):/foo -w /foo lambci/lambda:build-python3.8 \
    pip install -r requirements.txt -t ${LIB_DIR}

zip -r layer.zip python
```
* And I like to use `vim layer.zip` to look at the contents 

#### Get lambda configuration details by boto
* Super handy
```python
client = boto3.client('lambda')
out = client.get_function_configuration(FunctionName='myAwesomeLambda',
                                        # Qualifier='99', # optional version.
                                        )

In [30]: list(out.keys())
Out[30]: 
['ResponseMetadata',
 'FunctionName',
 'FunctionArn',
 'Runtime',
 'Role',
 'Handler',
 'CodeSize',
 'Description',
 'Timeout',
 'MemorySize',
 'LastModified',
 'CodeSha256',
 'Version',
 'VpcConfig',
 'Environment',
 'TracingConfig',
 'RevisionId',
 'Layers',
 'State',
 'LastUpdateStatus',
 'PackageType']
```
