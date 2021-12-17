---
layout: bc
title: Class Removed
---

> A class is removed from the library.

---

## Example
The `Person` class has been removed from the library project.

```diff 
-public class Person { }
```

---

## Broken Uses
Hereafter, we list the **broken uses** that are currently detected by Maracas.

- [1. References to the now-removed class](#case-1)

<br>

### 1. References to the now-removed class <a name="case-1"></a>
#### Example
The `Team` class—defined in a client project—declares the `members` field of type `List<Person>`.
The constructor of the class receives a list of `Person` objects as parameter.
Then, broken uses are reported pointing to the field declaration and the constructor declaration.

```java
public class Team {
  // Broken use reported here
  List<Person> members;

  // Broken use reported here
  public Team(List<Person> members) {
    this.members = members;
  }

}
```
