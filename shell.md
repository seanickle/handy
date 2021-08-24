
#### Edit files in place
* This `-i ''` was necessary on _MacOs_ to avoid creating a `greatings.txt.bak` file as a backup

```bash
$ sed -i '' 's/hello/bonjour/' greetings.txt
```

#### Xargs into vim
* Per [this helpful answer](https://unix.stackexchange.com/a/44428)  , you can `xargs` into `vim` on macos x with `xargs -o`  ...  

```
find . -name 'blahfile*py' |head -1 |xargs -o vim 
```


