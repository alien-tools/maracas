echo vytvoreni signature souboru:
java -cp jour-instrument-2.0.3.jar;javassist.jar net.sf.jour.SignatureGenerator --src testing-lib-v1-0.0.1.jar -jars ../lib_dependencies/rt.jar --packages testing_lib --dst sigTestLib1ApiSignature.xml --level private

echo Spusteni testu:
java -cp jour-instrument-2.0.3.jar;javassist.jar net.sf.jour.SignatureVerify --src testing-lib-v2-0.0.2.jar -jars ../lib_dependencies/rt.jar --signature sigTestLib1ApiSignature.xml --level private > jourReport.txt
