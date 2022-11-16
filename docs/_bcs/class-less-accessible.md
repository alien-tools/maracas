---
layout: bc
title: Class Less Accessible
---

> The visibility of a library class is decreased by modifying its access modifier.

---

## Example
The `public` access modifier of the top-level `Person` class in the library project has been removed.

```diff
-public class Person { }
+class Person { }
```

---

## Broken Uses
Hereafter, we list the **broken uses** that are currently detected by Maracas.

- [1. References to a now-private class](#case-1)
- [2. References to a now-package-private class from a different package](#case-2)
- [3. References to a now-protected class from a different package or a non-child class](#case-3)

<br>

### 1. References to a now-private class <a name="case-1"></a>
#### Example
The `public` access modifier of the inner `Person` class in the library project is changed to `private`.

```diff
public Organization {
- public class Person { }
+ private class Person { }
}

```

The `Team` class—defined in a client project—declares the `members` field of type `List<Organization.Person>`.
The constructor of the class receives a list of `Organization.Person` objects as parameter.
Then, broken uses are reported pointing to the field declaration and the constructor declaration.

```java
public class Team {
  // Broken use reported here
  List<Organization.Person> members;

  // Broken use reported here
  public Team(List<Organization.Person> members) {
    this.members = members;
  }

}
```

---
### 2. References to a now-package-private class from a different package <a name="case-2"></a>
#### Example
The `public` access modifier of the top-level `Person` class in the library project has been removed.

```diff
-public class Person { }
+class Person { }
```

The `Team` class—defined in a client project—declares the `members` field of type `List<Person>`.
The constructor of the class receives a list of `Person` objects as parameter.
Then, broken uses are reported pointing to the field declaration and the constructor declaration given that the `Team` class is not located in the same package as the `Person` class.

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

---
### 3. References to a now-protected class from a different package or a non-child class <a name="case-3"></a>
#### Example
The `public` access modifier of the inner `Person` class in the library project is changed to `protected`.

```diff
public Organization {
- public class Person { }
+ protected class Person { }
}

```
The `Team` class—defined in a client project—declares the `members` field of type `List<Organization.Person>`.
The constructor of the class receives a list of `Organization.Person` objects as parameter.
Then, broken uses are reported pointing to the field declaration and the constructor declaration given that the `Team` class is not a subtype of `Organization`.

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
