---
layout: bc
title: Field Removed
---

> A field is removed from its owning class.

---

## Example
The field `name` is removed from the class `Person` in the library project.

```diff
public class Person {
-  public String name;

}
```
---

## Broken Uses
Hereafter, we list the **broken uses** that are currently detected by Maracas.   

- [1. Read accesses of the now-removed field](#case-1)
- [2. Write accesses of the now-removed field](#case-2)

<br>

### 1. Read accesses of the now-removed field <a name="case-1"></a>
#### Example

The method `displayPerson()`—defined in the `PeopleInfo` class in a client project—accesses the `name` field.
Then, a broken use is reported pointing to the field access statement.

```java
public class PeopleInfo {
  public void displayPerson(Person person) {
    // Broken use due to Sa field removed change
    String personName = person.name;
    System.out.println(personName);
  }

}
```


### 2. Write accesses of the now-removed field <a name="case-2"></a>
#### Example
The method `addLastname()`—defined in the `PeopleInfo` class in a client project—accesses the `name` field.
Then, a broken use is reported pointing to the field access statement.

```java
public class PeopleInfo {
  public void addLastname(Person person, String lastname) {
    // Broken use due to a field removed change
    person.name += " " + lastname;
  }

}
```
