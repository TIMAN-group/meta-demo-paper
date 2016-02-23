paper.pdf: $(wildcard *.tex) *.bib
	latexmk paper.tex --shell-escape -pdf -dvi- -ps-

clean:
	latexmk -c
	latexmk -CA
	rm paper.bbl
