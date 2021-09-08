
#### for tar.gz
Assuming we have a func `read_from_s3` that reads from s3...

```python
import tarfile

targz = read_from_s3(bucket, s3fn)
tar = tarfile.open(fileobj=io.BytesIO(targz), mode="r:gz")
blahstream = tar.extractfile('blah-filename')

```

#### for zip files 
* Nice doc [here](https://medium.com/dev-bits/ultimate-guide-for-working-with-i-o-streams-and-zip-archives-in-python-3-6f3cf96dca50)

```python
from zipfile import ZipFile

with ZipFile('foo.zip') as zip_archive:
  foo = zip_archive.read('some/file.txt')

```
