
#### ag the silver searcher
* [here](https://geoff.greer.fm/2011/12/27/the-silver-searcher-better-than-ack/)
* usually `the_silver_searcher` on homebrew

#### jq
* command line json parsing 
* [here](https://stedolan.github.io/jq/download/)

#### markdown to pdf
* With [pandoc](https://pandoc.org) (I used `brew install pandoc` ).
* And [thanks stackoverflow](https://stackoverflow.com/a/55484165/472876)  , `pandoc MANUAL.md -o example13.pdf`
* [documentation](https://pandoc.org/MANUAL.html)  
* from markdown to latex

```
pandoc -f markdown -t latex hello.txt
```
* Even `--toc, --table-of-contents`  , `--toc-depth=NUMBER`  , for automatic table of contents. 

* Specifically for double spaced output pdf [this stackoverflow](https://stackoverflow.com/a/14972019/472876) tip was amazing, to create a file like `options.sty` 
 
``` 
 \usepackage{setspace}
 \doublespacing
 \usepackage[vmargin=1in,hmargin=1in]{geometry}
 \usepackage{lineno}
 \linenumbers) 
```

And then use `pandoc -H options.sty blah.md -o blah.pdf` . Amazing! 

#### change margins 
* Per this [really cool answer](https://stackoverflow.com/questions/13515893/set-margin-size-when-converting-from-markdown-to-pdf-with-pandoc) , here's a quick way to change margins to `1inch`

```
pandoc -V geometry:margin=1in -o output.pdf input.md
```
* Not sure yet how this is used by LaTeX , but you can also add this as the markdown front matter too

```

Recent versions of rmarkdown and pandoc

In more recent versions of rmarkdown, the settings of margins can be done in the YAML header via the top-level element geometry. What you specify in the geometry tag will be piped into the LaTeX template that ships with Pandoc via the following LaTeX snippet

$if(geometry)$
\usepackage[$for(geometry)$$geometry$$sep$,$endfor$]{geometry}
$endif$

For example, to specify margins that are 2cm in width one would include

---
title: "Habits"
author: John Doe
date: March 22, 2005
geometry: margin=2cm
output: pdf_document
---

For more complex specifications to be passed to the geometry LaTeX package, string options together as you would with LaTeX:

---
title: "Habits"
author: John Doe
date: March 22, 2005
geometry: "left=3cm,right=3cm,top=2cm,bottom=2cm"
output: pdf_document
---

```

#### pdf for kindle !!
* Equally as amazing is the [k2pdf](https://www.willus.com/k2pdfopt/) software which restructures pdf files to comfortable kindle viewing. This is absolutely mind blowing work!
