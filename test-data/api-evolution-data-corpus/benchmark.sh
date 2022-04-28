#!/usr/bin/env bash

CSV_FILE="compatibility.csv"
CSV_FILE_TMP="compatibility.csv.tmp"
CSV_BENCHMARK_FILE="benchmark.csv"

## return "1" if incompatibility was detected in the row
## detected incompatibility is found by grep pattern matching
function incompatibilityDetected() {

    report="tools/.reports/$1"

    if grep -q "$2" $report ; then echo 1
    else echo 0 ; fi
}


# make sure the compatibility table is generated
#./compatibility.sh

cd tools
# make sure the reports are generated
./run.sh

# All tools.
TOOL_REPORTS=()

# iterate reports and get report names from file-names
for d in .reports/* ; do

    # cut only file name
    filename=$(basename "$d")
    TOOL_REPORTS+=("$filename")
done

cd ..


#####
# Caution: we grep only incompatible results
# The reason is: a test scenario could actually pass source/binary compatibility check
####

# cp $CSV_FILE $CSV_BENCHMARK_FILE
# header
echo "change,source,binary" > $CSV_BENCHMARK_FILE
# compatible results (it has at least one "0" in the row
grep "0" $CSV_FILE >> $CSV_BENCHMARK_FILE
# source incompatible only
#grep "0,1" $CSV_FILE >> $CSV_BENCHMARK_FILE
# binary incompatible + any source 
#grep '.*,0$' $CSV_FILE >> $CSV_BENCHMARK_FILE

# iterate tools
for filename in "${TOOL_REPORTS[@]}" ; do

    rm -f $CSV_FILE_TMP

    # iterate compatibility.csv
    while read line; do
    change=`echo $line | cut -d, -f1`

    toolName=$(echo $filename | cut -f 1 -d '.')

    if [ "$change" = "change" ]
    then value="$toolName"
    else value=$(incompatibilityDetected $filename $change)
    fi

    echo "${line},${value}" >> $CSV_FILE_TMP

    done <$CSV_BENCHMARK_FILE

    cp $CSV_FILE_TMP $CSV_BENCHMARK_FILE


done

rm $CSV_FILE_TMP
