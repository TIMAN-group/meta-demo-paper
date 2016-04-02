CP=-cp lucene-5.5.0/core/lucene-core-5.5.0.jar:lucene-5.5.0/analysis/common/lucene-analyzers-common-5.5.0.jar:lucene-5.5.0/queryparser/lucene-queryparser-5.5.0.jar:./ireval:.

all:
	javac $(CP) *.java ireval/*.java

index-blog06:
	java $(CP) Index blog06

index-gov2:
	java $(CP) Index gov2

search-blog06:
	java $(CP) Search blog06 851

search-gov2:
	java $(CP) Search gov2 701
