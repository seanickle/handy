
### State Machine life cycle
* Adapting this from [my stackoverflow answer](https://stackoverflow.com/questions/43129455/how-do-i-modify-an-aws-step-function) to a question on updating state machines in step functions 



#### `step_function_stack.yaml`

```yaml
AWSTemplateFormatVersion: '2010-09-09'
Transform: 'AWS::Serverless-2016-10-31'
Description: >-
  A description of the State Machine goes here. 
Resources:
  MyStateMachineName:
    Type: AWS::StepFunctions::StateMachine
    Properties:
      RoleArn: "arn:aws:iam::{{aws_account_id}}:role/service-role/StepFunctions-MyStepFunctionRole"

      StateMachineName: "MyStateMachineName"
      StateMachineType: "EXPRESS"

      DefinitionString:
        Fn::Sub: |
          {{full_json_definition}}
```

#### `manage_step_functions.py`

```python
import boto3
import os
import time
from jinja2 import Environment


def do_render(full_json_definition):
    with open('step_function_stack.yaml') as fd:
        template = fd.read()
    yaml = Environment().from_string(template).render(
            full_json_definition=full_json_definition,
            aws_account_id=os.getenv('AWS_ACCOUNT_ID')) 
    return yaml



def update_step_function(stack_name, full_json_definition,):
    yaml = do_render(full_json_definition)
    client = boto3.client('cloudformation')
    response = client.update_stack(
        StackName=stack_name,
        TemplateBody=yaml,
        Capabilities=[
            'CAPABILITY_AUTO_EXPAND',
        ])
    return response


def create_step_function(stack_name, full_json_definition,):
    yaml = do_render(full_json_definition)
    client = boto3.client('cloudformation')
    response = client.update_stack(
        StackName=stack_name,
        TemplateBody=yaml,
        Capabilities=[
            'CAPABILITY_AUTO_EXPAND',
        ])
    return response


  def get_lambdas_stack_latest_events(stack_name):
      # Get the first 100 most recent events.
      client = boto3.client('cloudformation')
      return client.describe_stack_events(
          StackName=stack_name)


  def wait_on_update(stack_name):
      events = None
      while events is None or events['StackEvents'][0]['ResourceStatus'] not in ['UPDATE_COMPLETE',
              'UPDATE_ROLLBACK_COMPLETE', 'DELETE_COMPLETE', 'CREATE_COMPLETE']:

          print(events['StackEvents'][0]['ResourceStatus'] if events else ...)
          events = get_lambdas_stack_latest_events(stack_name)
          time.sleep(1)

      return events 
```

#### step_function_definition.json

```json
{
  "Comment": "This is a Hello World State Machine from https://docs.aws.amazon.com/step-functions/latest/dg/getting-started.html#create-state-machine",
  "StartAt": "Hello",
  "States": {
    "Hello": {
      "Type": "Pass",
      "Result": "Hello",
      "Next": "World"
    },
    "World": {
      "Type": "Pass",
      "Result": "World",
      "End": true
    }
  }
}
```

#### Create that step function

```python
# From a python shell for example
# First just set any privileged variables through environmental variables so they are not checked into code
# export AWS_ACCOUNT_ID=999999999

# edit step_function_definition.json then read it
with open('step_function_definition.json') as fd:
    step_function_definition = fd.read()

import manage_step_functions as msf
stack_name = 'MyGloriousStepFuncStack'
msf.create_step_function(stack_name, step_function_definition)

# If you are ready to update your State Machine, 
#   you can edit step_function_definition.json or you might create a new file for reference,
#   step_function_definition-2021-01-29.json
#   (Because at time of this writing Step Functions dont have versions like Lambda for instance)

with open('step_function_definition-2021-01-29.json') as fd:
    step_function_definition = fd.read()

msf.update_step_function(stack_name, step_function_definition)

   
```