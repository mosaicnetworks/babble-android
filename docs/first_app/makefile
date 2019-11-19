all: pdf html latex docx rst epub  gfm s5

pdf:
	pandoc -V papersize:a4 --toc -f markdown-implicit_figures    -o first.pdf my_first_babble_app.md 

html:
	pandoc -V papersize:a4 --toc -f markdown-implicit_figures -s   -o first.html my_first_babble_app.md 


latex:
	pandoc -V papersize:a4 --toc -f markdown-implicit_figures    -o first.latex my_first_babble_app.md 

docx:
	pandoc -V papersize:a4 --toc -f markdown-implicit_figures    -o first.docx my_first_babble_app.md 


epub:
	pandoc -V papersize:a4 --toc -f markdown-implicit_figures    -o first.epub my_first_babble_app.md 



rst:
	pandoc -V papersize:a4 --toc -f markdown-implicit_figures    -o first.rst my_first_babble_app.md 



gfm:
	pandoc -V papersize:a4 --toc -f markdown-implicit_figures    -o first.md my_first_babble_app.md 





s5:
	pandoc -V papersize:a4 --toc -f markdown-implicit_figures -s  -t s5  -o first.slidy.html my_first_babble_app.md 



