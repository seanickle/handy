

### Checking sha256

#### For older versions of sshd
```
awk '{print $2}' /etc/sshd/ssh_host_rsa_key.pub | base64 -d | sha256sum -b | awk '{print $1}' | xxd -r -p | base64 
```

#### Newer sshd
```
ssh-keygen -l -f key.pub -E (sha256|md5)
```


###  ssh tunnel

Here's a `tunnel_script.sh` , 

```sh
# Common
destination_port=5432
# (5) 
db=my-awesome-db.foo.com
local_port=55001

tunnel_box=my-bastion-box

# XXX XXX XXX 
echo "Run tunnel on  ${local_port}:${db}:${destination_port} on \"${tunnel_box}\"."
ssh -N -L ${local_port}:${db}:${destination_port} ${tunnel_box}
echo "..."
# XXX XXX XXX 
```
