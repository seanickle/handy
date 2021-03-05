

### Checking sha256

#### For older versions of sshd
```
awk '{print $2}' /etc/sshd/ssh_host_rsa_key.pub | base64 -d | sha256sum -b | awk '{print $1}' | xxd -r -p | base64 
```

#### Newer sshd
```
ssh-keygen -l -f key.pub -E (sha256|md5)
```
