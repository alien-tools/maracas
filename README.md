![Build](https://github.com/alien-tools/maracas/workflows/Java%20CI/badge.svg?branch=main) ![CodeQL](https://github.com/alien-tools/maracas/workflows/CodeQL/badge.svg?branch=main)  [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT) [![Contributor Covenant](https://img.shields.io/badge/Contributor%20Covenant-2.1-4baaaa.svg)](code_of_conduct.md)

# Maracas

Maracas is a source code and bytecode analysis framework—written in Java with the help of [Spoon](https://github.com/INRIA/Spoon)—designed to analyze how Java libraries evolve and how their evolution impact their clients.

Currently, Maracas consists of three main components:
  - The [core API](core/) computes the list of changes between two binary versions of a library (using [japicmp](https://github.com/siom79/japicmp) under the hood) and the impact these changes have on client code
  - The [forges API](forges/) handles communication with software forges (currently GitHub only) and build systems (currently Maven and Gradle) to gather source code and build JARs that are then analyzed by the core API
  - The [REST API](rest/) exposes a set of REST endpoints that make it easy to ask Maracas to analyze library versions and clients. In particular, it is used by [BreakBot](https://github.com/alien-tools/breakbot) to analyze pull requests on GitHub and report their impact

## Content

- [Using Maracas](#using-maracas)
- [Deploying Maracas REST](#deploying-maracas-rest)
- [Documentation](#documentation)
- [Support](#support)
- [Contributing](#contributing)
- [License](#license)


## Using Maracas

### Dependency

`maracas-core` is deployed on GitHub Packages.
First, configure [Maven](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry) or [Gradle](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry) to work with GitHub Package.
Then, declare the following dependency:

```xml
<dependency>
  <groupId>com.github.maracas</groupId>
  <artifactId>maracas-core</artifactId>
  <version>0.4.0</version>
</dependency>
```

### As an API
One may use Maracas to compute the changes between two versions of a library as well as their impact on a particular client as follows.

*Note that both versions of the library must be provided as binary JARs, while the client is provided as source code.*

```java
// Setting up the library versions and clients
LibraryJar v1 = new LibraryJar(Path.of("v1.jar"));
LibraryJar v2 = new LibraryJar(Path.of("v2.jar"));
SourcesDirectory client = new SourcesDirectory(Path.of("/path/to/client"));

// Using a query/result
AnalysisQuery query = AnalysisQuery.builder()
  .oldVersion(v1)
  .newVersion(v2)
  .client(client)
  .build();

AnalysisResult result = Maracas.analyze(query);
Delta delta = result.delta();
Set<BrokenUse> brokenUses = result.allBrokenUses();

// Or by directly invoking the analysis methods
Delta delta = Maracas.computeDelta(v1, v2);
Collection<BreakingChange> breakingChanges = delta.getBreakingChanges();

DeltaImpact deltaImpact = Maracas.computeDeltaImpact(client, delta);
Set<BrokenUse> brokenUses = deltaImpact.getBrokenUses();

// Delta models are built from JARs and lack source code locations.
// To map breaking changes to precise locations in source code,
// create a library jar with its corresponding source code
LibraryJar v1 = new LibraryJar(Path.of("v1.jar"),
	new SourcesDirectory(Path.of("/path/to/v1/src")));
```

### From the command line
Alternatively, one can invoke Maracas from the command line using the provided CLI.
First, build a standalone JAR from Maracas Core, and then follow the `--help` guidelines:

```bash
$ cd core/
$ mvn clean compile assembly:single
$ java -jar target/maracas-core-<version>-jar-with-dependencies.jar --help
```

The example above can be invoked from the CLI as follows:

```bash
$ java -jar target/maracas-core-<version>-jar-with-dependencies.jar --old v1.jar --new v2.jar --client /path/to/client/src/main/java
```

## Deploying Maracas REST

### Configuration
As Maracas REST needs to interact with the GitHub REST API, one must first configure a [personal token](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token) to be used.
To do so, a file named `.github` must be placed in the `rest/src/main/resources/` directory with the following content:

```bash
$ cat rest/src/main/resources/.github
oauth=<GITHUB_TOKEN>
```

### Execution
The preferred way to run the Maracas REST server is using Docker:
```bash
$ cd rest/
$ docker-compose build
$ docker-compose up
```

The REST server listens on port `8080`. Its documentation is exposed at `http://localhost:8080/swagger-ui/index.html?configUrl=/api-docs/swagger-config`.


## Documentation
To learn more about Maracas and how to use it, please visit our [GitHub page](https://alien-tools.github.io/maracas/).


## Support
If you would like to learn more about Maracas or you are a current user and you need some help, do not hesitate to send us an email at [thomas.degueule \<at> labri.fr](mailto:thomas.degueule@labri.fr?subject=[Maracas]%20Support) or [l.m.ochoa.venegas \<at> tue.nl](mailto:l.m.ochoa.venegas@tue.nl?subject=[Maracas]%20Support).


## Contributing
To learn more about how to contribute to Maracas, please check the provided [guidelines](https://github.com/alien-tools/maracas/blob/main/CONTRIBUTING.md) and the project [code of conduct](https://github.com/alien-tools/maracas/blob/main/CONTRIBUTING.md).

## License
This repository—and all its content—is licensed under the [MIT License](https://choosealicense.com/licenses/mit/).  
© 2021 Maracas
