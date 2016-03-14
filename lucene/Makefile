CP=-cp lucene-5.5.0/core/lucene-core-5.5.0.jar:lucene-5.5.0/analysis/common/lucene-analyzers-common-5.5.0.jar:lucene-5.5.0/queryparser/lucene-queryparser-5.5.0.jar:.

all:
	javac $(CP) *.java

index:
	java $(CP) Index

search:
	java $(CP) Search
