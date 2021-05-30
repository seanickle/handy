* Download MacVIM from [macvim github](https://github.com/macvim-dev/macvim/releases) ( which was forked from [here](https://github.com/b4winckler/macvim) originally I think ) 

```
mkdir -p ~/.vim/pack/themes/opt/

cd ~/.vim/pack/themes/opt/
git clone git@github.com:lifepillar/vim-solarized8.git

```


### Vim notes
* Vim doesnt know about "terraform" files like `.tf` and [the hashivim github](https://github.com/hashivim/vim-terraform) helps with that. `vim-terraform` actually has a magical `:TerraformFmt` command that inspects the syntax of your `.tf` file too.
