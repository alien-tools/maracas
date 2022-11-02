---
layout: bc
title: Field Less Accessible
---

> The visibility of a library field is decreased by modifying its access modifier.

---

## Example
The access modifier of the field `name` in the library class `Person` is changed from `public` to `protected`.

```diff
public class Person {
-  public String name;
+  protected String name;
}
```
---

## Broken Uses
Hereafter, we list the **broken uses** that are currently detected by Maracas.   

- [1. Field accesses of a now-private field](#case-1)
- [2. Field accesses of a now-package-private field from a different package](#case-2)
- [3. Field accesses of a now-protected field from a different package or a non-child class](#case-3)

<br>

### 1. Field accesses of a now-private field <a name="case-1"></a>
#### Example
The access modifier of the field `name` in the library class `Person` is changed from `public` to `private`.

```diff
public class Person {
-  public String name;
+  private String name;
}
```

The method `displayPerson`—defined in the `PeopleInfo` class in a client project—accesses the now-private `name` field.
Then, a broken use is reported pointing to the field access expression.

```java
public class PeopleInfo {
  public void displayPerson(Person person) {
    // Broken use reported here
    String personName = person.name;
    System.out.println(personName);
  }

}
```
---

### 2. Field accesses of a now-package-private field from a different package <a name="case-2"></a>
#### Example
The access modifier of the field `name` in the library class `Person` is changed from `public` to package-private (by removing the modifier).

```diff
public class Person {
-  public String name;
+  String name;
}
```

The method `displayPerson`—defined in the `PeopleInfo` class in a client project—accesses the now-package-private `name` field.
Then, a broken use is reported pointing to the field access expression.

```java
public class PeopleInfo {
  public void displayPerson(Person person) {
    // Broken use reported here
    String personName = person.name;
    System.out.println(personName);
  }

}
```
### 3. Field accesses of a now-protected field from a different package or a non-child class <a name="case-3"></a>
#### Example
The access modifier of the field `name` in the library class `Person` is changed from `public` to `protected`.

```diff
public class Person {
-  public String name;
+  protected String name;
}
```

The method `displayPerson`—defined in the `PeopleInfo` class in a client project—accesses the now-protected `name` field.
Then, a broken use is reported pointing to the field access expression.

```java
public class PeopleInfo {
  public void displayPerson(Person person) {
    // Broken use reported here
    String personName = person.name;
    System.out.println(personName);
  }

}
```
