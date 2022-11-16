---
layout: bc
title: Field Now Final
---

> The `final` modifier is added to the declaration of a library field.

---

## Example
The `final` modifier is added to the declaration of the field `name` in the library class `Person`.

```diff
public class Person {
-  public String name;
+  public final String name;

  public Person(String name) {
    this.name = name
  }
}
```
---

## Broken Uses
Hereafter, we list the **broken uses** that are currently detected by Maracas.   

- [1. Write accesses of the now-final field](#case-1)

<br>

### 1. Write accesses of the now-final field <a name="case-1"></a>
#### Example
The method `addLastName`—defined in the `PeopleInfo` class in a client project—accesses the `name` field.
Then, a broken use is reported pointing to the field access expression given that the value of `name` has already been initialized when instantiating the object.

```java
public class PeopleInfo {
  public void addLastName(Person person, String lastname) {
    // Broken use reported here
    person.name += " " + lastname;
  }

}
```
