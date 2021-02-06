
#### Edit files in place
* This `-i ''` was necessary on _MacOs_ to avoid creating a `greatings.txt.bak` file as a backup

```bash
$ sed -i '' 's/hello/bonjour/' greetings.txt
```
