
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
docker run -i -t -v $(pwd)/local/path:/docker/path name-of-image


# or with a specific image id...
docker run -d=false  -i -t ad6576e  
```
