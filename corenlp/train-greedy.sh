#!/bin/bash
set -eo pipefail

treebank=/home/chase/projects/meta-data/penn-treebank/treebank-3/parsed/mrg/wsj
# usage is https://github.com/kpu/usage
usage=/home/chase/projects/usage/
corenlp=./stanford-corenlp-full-2015-12-09

$usage/measure.sh \
  java -mx14g -cp "$corenlp/*" \
    edu.stanford.nlp.parser.shiftreduce.ShiftReduceParser \
    -trainTreebank $treebank 200-2199 \
    -devTreebank $treebank 2200-2219 \
    -serializedPath sr-parser-greedy.ser.gz \
    -trainingThreads 12 \
    -trainingMethod EARLY_TERMINATION
