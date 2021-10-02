
* I Stumbled on [this gem of a stack overflow answer](https://unix.stackexchange.com/a/9499)  (  in [here](https://unix.stackexchange.com/questions/9496/looping-through-files-with-spaces-in-the-names#9499) ) , which goes into some great detail but one of the hacks I learned is you can modify `IFS` to change the following behavior. 
* (For context, I have some files   with spaces in the names and they end in `RR.jpg` . 

```
$ for file in $(ls *RR.jpg) ; do echo $file ; done
2021-05-19
09.01.51RR.jpg
2021-05-19
09.02.00RR.jpg
2021-05-19
09.02.07RR.jpg
$ echo $IFS

$ OIFS="$IFS"
$ IFS=$'\n'
$ for file in $(ls *RR.jpg) ; do echo $file ; done
2021-05-19 09.01.51RR.jpg
2021-05-19 09.02.00RR.jpg
2021-05-19 09.02.07RR.jpg
$ IFS="$OIFS"
$ for file in $(ls *RR.jpg) ; do echo $file ; done
2021-05-19
09.01.51RR.jpg
2021-05-19
09.02.00RR.jpg
2021-05-19
09.02.07RR.jpg


```
