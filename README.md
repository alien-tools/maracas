# Maracas
Maracas is a source code and bytecode analysis framework⁠—written in Java with the help of [Spoon](https://github.com/INRIA/Spoon)—designed to analyze how Java libraries evolve and how their evolution impact their clients.

Currently, Maracas consists of two main components:
  - The [core API](core/) computes the list of changes between two binary versions of a library (using [japicmp](https://github.com/siom79/japicmp) under the hood) and the impact these changes have on client code
  - The [REST API](rest/) exposes a set of REST endpoints that make it easy to ask Maracas to analyze library versions and clients. In particular, it is used by [break-bot](https://github.com/break-bot/breakbot) to analyze pull requests on GitHub and report their impact

## Getting started
One may use Maracas to compute the changes between two versions of a library as well as their impact on a particular client as follows:

```java
Path v1 = Paths.get("v1.jar");
Path v2 = Paths.get("v2.jar");
Path c =  Paths.get("/path/to/client/src/main/java");

MaracasQuery query =
  new MaracasQuery.Builder()
    .v1(v1.toAbsolutePath())
    .v2(v2)
    .client(c)
    .build();

MaracasResult result = new Maracas().analyze(query);
System.out.println("Changes: " + result.delta());
System.out.println("Impact:  " + result.allDetections());
```
