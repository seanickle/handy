### Using a docker based approach to "ECS Execute" into a  cluster task container

#### What is ECS Execute
* This is a cool command you can use to get a shell on an ECS cluster container
* But this requires AWS CLI version 2 , which cannot be installed like `pip install awscli` 
* But instead you can use a docker image conveniently ... (below)
 
#### First create a new docker image with the below Dockerfile 

Note that as of this writing the `amazon/aws-cli` image does not have the so called "session manager plugin" and so someone here, https://github.com/aws/aws-cli/issues/5373  , has suggested to just add a one liner like this , 

```
FROM amazon/aws-cli

RUN curl "https://s3.amazonaws.com/session-manager-downloads/plugin/latest/linux_64bit/session-manager-plugin.rpm" -o "session-manager-plugin.rpm" && \
    yum install -y ./session-manager-plugin.rpm
```


And then build .... 

```
docker build -t awscli-sesh -f /path/to/this/new/Dockerfile . 
```

#### What IAM user to use ? 
* So I have a  profile in my `~/.aws/credentials` called `[mymfa]` , which uses "MFA"  . 



#### generate a session for MFA/aws cli
Per this cool  AWS doc , https://aws.amazon.com/premiumsupport/knowledge-center/authenticate-mfa-cli/  , this is the basic form to get a token 

```
aws sts get-session-token --serial-number arn-of-the-mfa-device --token-code code-from-token
```

So using  my virtual MFA ...  I run this locally , using the TOTP from my virtual  mfa , 

```
aws --profile mymfa sts get-session-token \
     --serial-number "arn:aws:iam::<account_id>:mfa/my.user.blah" \
     --token-code "123456" --duration-seconds 900
```

Then the output of that looks like 

```
{
    "Credentials": {
        "AccessKeyId": "...",
        "SecretAccessKey": "..",
        "SessionToken": "...",
        "Expiration": "2021-11-02T19:16:51Z"
    }
}
```

I used these to create a new temporary profile , `[mysession]` in my `~/.aws/credentials` , 

```
[mysession]
region = my-region-1
aws_access_key_id = example-access-key-as-in-returned-output
aws_secret_access_key = example-secret-access-key-as-in-returned-output
aws_session_token = example-session-Token-as-in-returned-output
```




#### And then finally my docker based aws cli worked ... 
Using a `<ecs_task_id>` from the corresponding  https://console.aws.amazon.com/ecs/home   cluster , replacing `"<your_cluster>"` ,  `"<ecs_task_id>"` and `"<the_name_of_the_container>"` with your own stuff . Note how `-v` is used to map your `~/.aws` to that on the docker container 


```sh
docker run --rm -it -v ~/.aws:/root/.aws  awscli-sesh --profile mysession \
            ecs   execute-command  --cluster "<your_cluster>" \
            --task "<ecs_task_id>"  \
            --container "<the_name_of_the_container>"     --interactive  \
            --command "/bin/bash"   
```

And that lets me run `/bin/bash` 


