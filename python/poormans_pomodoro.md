

```python
import sys
import time 
import datetime 
file = sys.stderr 
def log(logfile, tag): 
    now = datetime.datetime.now().strftime('%Y-%m-%d %H:%M EST') 
    with open(logfile, 'a') as fd: 
        fd.write(f'{now} {tag}\n') 
def do(minutes, logfile, tag): 
    log(logfile, f'{tag} start') 
    seconds = minutes*60 
    for i in range(seconds): 
        file.flush() 
        #s = str(i%60).zfill(2) 
        file.write(f'\r{i//60}:{str(i%60).zfill(2)}') 
        time.sleep(1) 
    log(logfile, f'{tag} end') 
    
```
And choose any `logfile` location and any `tag` ..
