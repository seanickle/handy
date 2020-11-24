
### Do a build
* login from shell, 
```
$(aws --profile my-local-aws-profile ecr get-login --no-include-email --region us-east-1)
```
* build,
```
docker build -t name-of-image -f path/to/Dockerfile path/to/docker/context
```

### Run your container
```
# run using an image name,
# note that -v takes an absolute path...
docker run -i -t -v $(pwd)/local/path:/docker/path <name-of-image>:<tag>


# or with a specific image id... say "ad6576e"
docker run -d=false  -i -t ad6576e  
```

#### If you need your container to have your aws creds
Nice hack is to map the "root" user of your container `.aws` directory 

```
docker run -i -t -v ~/.aws:/root/.aws -v $(pwd)/local/path:/docker/path <name-of-image>:<tag>

```
* ( cool idea from a colleague ^ ) 
