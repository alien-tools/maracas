---
layout: bc
title: Class Now Abstract
---

> The `abstract` modifier is added to the declaration of a library class, or the class is changed to an interface.

---

## Example
The `abstract` modifier is added to the declaration of the `Person` class in the library project.

```diff
-public class Person {
+public abstract class Person {
  String name;
  int age;
}
```

---

## Broken Uses
Hereafter, we list the **broken uses** that are currently detected by Maracas.

- [1. Instantiation of the now-abstract class](#case-1)

<br>

### 1. Instantiation of the now-abstract class <a name="case-1"></a>
#### Example
The `Team` class—defined in a client project—declares the `members` field of type `List<Person>`.
The class has a method `addMember` that receives the name and the age of the new team member, creates an object of `Person` type, and adds it to the `members` field.
Then, a broken use is reported pointing to the `Person` object instantiation.

```java
public class Team {
  List<Person> members;

  // Broken use reported here
  public addMember(String name, int age) {
    Person member = new Person(name, age);
    members.add(member)
  }

}
```
