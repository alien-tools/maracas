# Maracas Test Data

To run the test cases located in the `com.github.maracas.validator.cases` package, you must specify the Maven application directory.

### In Eclipse
1. Got to `Run > Run Configurations... > Java Application > New Configuration`.
1. Specify the name of the configuration (e.g. `APIEvolutionDataCorpusCase`).
1. Point to the `maracas-validatr` project.
1. Choose the main class of the case you want to run (e.g. `com.github.maracas.validator.cases.APIEvolutionDataCorpusCase`).
1. Leave the other options empty.
1. Click on the `Environment` tab and then on `Add` to create a new environment variable.
1. Use `M2_HOME` as name of the variable and write down the path to the Maven folder as value (e.g. `usr/share/maven`). 
