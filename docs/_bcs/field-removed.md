---
layout: bc
title: Field Removed
---

> A field is removed from its owning class.

Hereafter, we list the **broken uses** that are currently detected by Maracas:

---

## 1. Read accesses of the now-removed field
### Example
The field `name` is removed from the class `Person` in the library project.
The method `displayPerson()`—defined in the `PeopleInfo` class in a client project—accesses the `name` field.
Then, a broken use is reported pointing to the field access statement.

**Old version of the library:**
```java
public class Person {
  public String name;
}
```

**New version of the library:**
```java
public class Person {
  // Field name has been removed
}
```

**Broken use in the client code:**
```java
public class PeopleInfo {
  public void displayPerson(Person person) {
    // Broken use due to a field removed change
    String personName = person.name;
    System.out.println(personName);
  }
}
```

---

## 2. Write accesses of the now-removed field
### Example
The field `name` is removed from the class `Person` in the library project.
The method `addLastname()`—defined in the `PeopleInfo` class in a client project—accesses the `name` field.
Then, a broken use is reported pointing to the field access statement.

**Old version of the library:**
```java
public class Person {
  public String name;
}
```

**New version of the library:**
```java
public class Person {
  // Field name has been removed
}
```

**Broken use in the client code:**
```java
public class PeopleInfo {
  public void addLastname(Person person, String lastname) {
    // Broken use due to a field removed change
    person.name += " " + lastname;
  }
}
```
