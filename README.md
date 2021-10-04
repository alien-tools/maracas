# Maracas
Maracas is a source code and bytecode analysis framework⁠—written in Java with the help of [Spoon](https://github.com/INRIA/Spoon)—designed to analyze how Java libraries evolve and how their evolution impact their clients.

Currently, Maracas consists of two main components:
  - The [core API](core/) computes the list of changes between two binary versions of a library (using [japicmp](https://github.com/siom79/japicmp) under the hood) and the impact these changes have on client code
  - The [REST API](rest/) exposes a set of REST endpoints that make it easy to ask Maracas to analyze library versions and clients. In particular, it is used by [break-bot](https://github.com/break-bot/breakbot) to analyze pull requests on GitHub and report their impact

## Using Maracas

### As an API
One may use Maracas to compute the changes between two versions of a library as well as their impact on a particular client as follows.

*Note that both versions of the library must be provided as binary JARs, while the client is provided as source code.*

```java
Path v1 = Paths.get("v1.jar");
Path v2 = Paths.get("v2.jar");
Path c =  Paths.get("/path/to/client/src/main/java");

// Using a query/result
AnalysisQuery query = AnalysisQuery.builder()
  .oldJar(v1)
  .newJar(v2)
  .client(c)
  .build();

AnalysisResult result = Maracas.analyze(query);
Delta delta = result.delta();
Collection<Detection> detections = result.allDetections();

// Programmatically
Delta delta = Maracas.computeDelta(v1, v2);
Collection<Detection> detections = Maracas.computeDetections(c, delta);
```

### From the command line
Alternatively, one can invoke Maracas from the command line using the provided CLI.
First, build a standalone JAR from Maracas Core, and then follow the `--help` guidelines:

```bash
cd core/
mvn clean compile assembly:single
java -jar target/maracas-core-<version>-jar-with-dependencies.jar --help
```

The example above can be invoked from the CLI as follows:

```bash
java -jar target/maracas-core-<version>-jar-with-dependencies.jar --old v1.jar --new v2.jar --client /path/to/client/src/main/java
```
