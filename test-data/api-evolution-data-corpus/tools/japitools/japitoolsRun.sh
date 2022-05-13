echo ********* Japitools *********
echo Creating signature files
japize as japitools/japizeSigFile packages ../../testing-lib-v1/target/testing-lib-v1-0.0.1.jar ../lib_dependencies/rt.jar +testing_lib
japize as japitools/japizeSigFile2 packages ../../testing-lib-v2/target/testing-lib-v2-0.0.2.jar ../lib_dependencies/rt.jar +testing_lib

echo comparing
japicompat -o japitoolsReport.txt japizeSigFile.japi.gz japizeSigFile2.japi.gz
