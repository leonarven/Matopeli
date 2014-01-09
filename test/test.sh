#!/bin/bash

cd ../bin;

if [ $# != 4 ] ; then
	echo "Usage test.sh [testName] [randSeed] [height] [width]";
	exit;
fi

java Matopeli $2 $3 $4 < ../test/syote_$1_$2_$3_$4.txt | diff -y ../test/tulos_$1_$2_$3_$4.txt -
