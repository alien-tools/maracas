![Build & Test](https://github.com/alien-tools/maracas/actions/workflows/build.yml/badge.svg?branch=main) ![CodeQL](https://github.com/alien-tools/maracas/actions/workflows/codeql-analysis.yml/badge.svg?branch=main) [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

**_You might want to check out [Roseau](https://github.com/alien-tools/roseau) for our latest work on breaking changes!_**

# Maracas

Maracas is a source code and bytecode analysis framework—written in Java with the help of [Spoon](https://github.com/INRIA/Spoon)—that tracks how Java libraries evolve and how their evolution impacts their clients. In a nutshell, Maracas makes it easy to:

  - Track the introduction of backward-incompatible breaking changes in your own APIs or the APIs you rely on
  - Analyze the concrete impact of these breaking changes on the client code using these APIs
  - Analyze local Java artifacts (source code and JARs) or analyze commits, branches, and pull requests remotely hosted on software forges such as GitHub. if you'd like to automatically check your pull requests for the introduction of breaking changes and their impact, you should give a try to our GitHub App [BreakBot](https://github.com/alien-tools/breakbot) :robot:!

Maracas consists of three main components:

  - The [core API](core/) identifies breaking changes between two binary versions of a library (using [japicmp](https://github.com/siom79/japicmp) under the hood) and the impact these changes have on client code
  - The [forges API](forges/) handles communication with software forges (currently GitHub only) and build systems (currently Maven and Gradle) to gather source code and build JARs that are then analyzed by the core API
  - The [REST API](rest/) makes it easy to ask Maracas to analyze library versions and clients. In particular, it is used by [BreakBot](https://github.com/alien-tools/breakbot) to analyze pull requests on GitHub.

## Content

- [Using Maracas](#using-maracas)
- [Deploying Maracas REST](#deploying-maracas-rest)
- [Documentation](#documentation)
- [Support](#support)
- [License](#license)


## Using Maracas

### Dependency

`maracas-core` is deployed on GitHub Packages.
First, configure [Maven](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry) or [Gradle](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry) to work with GitHub Package.
Then, include the following dependency:

```xml
<dependency>
  <groupId>com.github.maracas</groupId>
  <artifactId>maracas-core</artifactId>
  <version>0.5.0</version>
</dependency>
```

### Analyzing local libraries and clients
One can use Maracas to compute the changes between two versions of a library as well as their impact on a particular client as follows.

*Note that both versions of the library must be provided as binary JARs, while the client is provided as source code.*

```java
Maracas maracas = new Maracas();

// Setting up the library versions and clients
LibraryJar v1 = LibraryJar.withSources(Path.of("v1.jar"), Path.of("v1-sources/"));
LibraryJar v2 = LibraryJar.withoutSources(Path.of("v2.jar"));
SourcesDirectory client = SourcesDirectory.of(Path.of("/path/to/client"));

// Option 1: using the query/result API
AnalysisQuery query = AnalysisQuery.builder()
  .of(v1, v2)
  .client(client)
  .build();

AnalysisResult result = maracas.analyze(query);
Delta delta = result.delta();
List<BreakingChange> breakingChanges = delta.getBreakingChanges();
Set<BrokenUse> brokenUses = result.allBrokenUses();

// Option 2: invoking the analyses directly
Delta delta = maracas.computeDelta(v1, v2);
Collection<BreakingChange> breakingChanges = delta.getBreakingChanges();

DeltaImpact deltaImpact = maracas.computeDeltaImpact(client, delta);
Set<BrokenUse> brokenUses = deltaImpact.brokenUses();
```

### Analyzing GitHub repositories

Alternatively, one can use the [forges API](forges/) to analyze artifacts hosted on GitHub.

```java
// See https://github-api.kohsuke.org/ to setup the GitHubBuilder
GitHubForge forge = new GitHubForge(GitHubBuilder.fromEnvironment().build());

// Option 1: analyzing a pull request
PullRequestAnalyzer analyzer = new PullRequestAnalyzer(forge);
PullRequest pr = forge.fetchPullRequest("owner", "library", 42);

PullRequestAnalysisResult result = analyzer.analyze(pr, MaracasOptions.newDefault());
List<BreakingChange> breakingChanges = result.breakingChanges();
Set<BrokenUse> brokenUses = result.brokenUses();

// Option 2: analyzing two arbitrary commits
CommitAnalyzer analyzer = new CommitAnalyzer();
Commit v1 = forge.fetchCommit("owner", "library", "sha-v1");
Commit v2 = forge.fetchCommit("owner", "library", "sha-v2");
Commit client = forge.fetchCommit("owner", "client", "sha-client");

AnalysisResult result = analyzer.analyzeCommits(new CommitBuilder(v1), new CommitBuilder(v2),
    List.of(new CommitBuilder(client)), MaracasOptions.newDefault());

List<BreakingChange> breakingChanges = result.delta().getBreakingChanges();
Set<BrokenUse> brokenUses = result.allBrokenUses();
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

The REST server listens on port `8080`. Its documentation is exposed at `http://localhost:8080/swagger-ui.html`.


## Documentation
To learn more about Maracas, please visit our [GitHub page](https://alien-tools.github.io/maracas/).


## Support
If you would like to learn more about Maracas or you are a current user and you need some help, do not hesitate to ask questions in issues or to get in touch with [Lina Ochoa](https://github.com/lmove) or [Thomas Degueule](https://github.com/tdegueul).

## License
This repository—and all its content—is licensed under the [MIT License](https://choosealicense.com/licenses/mit/).  
