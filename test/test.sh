#!/bin/bash

if [ $# = 0 ] ; then
	diff_mode="-q";
	$0 hetipois 1 5 10 $diff_mode
	$0 kierto1 1 4 11 $diff_mode
	$0 kierto2 63 13 7 $diff_mode
	$0 liiku 1 5 10 $diff_mode
	$0 pakita 14 5 7 $diff_mode
	$0 silmukka1 5 3 10 $diff_mode
	$0 silmukka2 2 7 7 $diff_mode
	$0 syo 1 5 10 $diff_mode
	$0 tayta 123 4 8 $diff_mode
	$0 tehtanto 1 5 10 $diff_mode
	$0 tormaa1 1 5 10 $diff_mode
	$0 tormaa2 1 5 10 $diff_mode
	$0 vaihda 1 5 10 $diff_mode
	$0 virhe1 1 5 x $diff_mode

	# Ei voida testata tällä skriptillä
	# $0 pakita virhe2 11 22 33 44
	exit;
fi


cd ../bin;
argsOK=0

if [ $# = 4 ] ; then
	argsOK=1;
	diff_mode="-u";
elif [ $# = 5 ] ; then
	argsOK=1;
	diff_mode=$5;
else
	echo "Usage test.sh [testName] [randSeed] [height] [width]";
	exit;
fi

echo -e "Testing test \"${1}\" (args $1 $2 $3) ... \c";

output=$(java Matopeli $2 $3 $4 < ../test/syote_$1_$2_$3_$4.txt | diff $diff_mode ../test/tulos_$1_$2_$3_$4.txt -)

if [ ${#output} = 0 ] ; then
	echo "DONE"
else 
	echo "FAILED"
	echo -e "${output}"
fi
