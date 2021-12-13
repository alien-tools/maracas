---
layout: page
title: About
---

## What Is Maracas?
Maracas is a source code and bytecode analysis framework⁠—written in Java with the help of Spoon—designed to analyze how Java libraries evolve and how their evolution impacts their clients.
Maracas is built atop JApiCmp, a static analysis tool that computes the delta between two JAR (Java ARchive) files of a Java project.
Thanks to JApiCmp, Maracas computes the list of breaking changes introduced between two versions of a library, and⁠—with the help of Spoon⁠—detects broken uses on client code.


## Related Projects
Below, we list some relevant projects that are being used by Maracas.
- [JApiCmp](https://github.com/siom79/japicmp): static analysis tool that computes a delta between two JARs.
- [Spoon](https://github.com/INRIA/spoon): static analysis tool that analyzes, rewrites, and transforms Java source code.

## Contributors

- Thomas Degueule
- Jean-Rémy Falleri
- Lina Ochoa Venegas
