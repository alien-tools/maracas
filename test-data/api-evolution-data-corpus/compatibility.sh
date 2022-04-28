#!/usr/bin/env bash

CSV_FILE="compatibility.csv"

function write_csv_row() {

    if grep -Fq "Compile failed;" source.txt
    then ss=0;
    else ss=1;
    fi

    if grep -Fq "Java Result: -1" binary.txt
    then bb=0;
    else bb=1;
    fi

    row="$1,$ss,$bb"
    echo $row >> $CSV_FILE
}

echo "change,source,binary" > $CSV_FILE
# make sure all is build
ant jar

# iterate dirs - each dir is one experiment
for d in client/src/*/ ; do

    # run experiment for each package
    filename=$(basename "$d")
    ant run-experiments -Dpackage="$filename"
    write_csv_row $filename
done
