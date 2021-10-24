
### A nice way to pass API Gateway stuff to a lambda as a payload
* With the below, this will include the input as the `body` 
* And the path params will go under `pathParams`
* And if you have a stage variable called `thisStage` , then that will be passed as well.

```json

    "application/x-amz-json-1.0" = <<EOF
{
  "body" : $input.json('$'),
  "method": "$context.httpMethod",
  "pathParams": {
    #foreach($param in $input.params().path.keySet())
    "$param": "$util.escapeJavaScript($input.params().path.get($param))" #if($foreach.hasNext),#end

    #end
  },
  "thisStage": "$${stageVariables.thisStage}"
}
EOF
  }

```
