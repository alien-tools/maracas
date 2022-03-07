---
layout: bc
title: Constructor Removed
---

> A constructor is removed from its owning class.

---

## Example
The constructor of the class `Person` in the library has been removed.

```diff
public abstract class Person {
  private String name;
  private int age;

-  public Person(String name, int age) {
-    this.name = name;
-    this.age = age;
-  }

  public abstract String display();

}
```

The constructor of the class `Team` in the library has been removed.

```diff
public class Team {
  List<Person> members;
 
-  public Team(List<Person> members) {
-    this.members = members;
-  }

}
```

---

## Broken Uses
Hereafter, we list the **broken uses** that are currently detected by Maracas.

- [1. Invocations to the now-removed constructor](#case-1)
- [2. Invocations to the now-removed constructor when creating anonymous classes](#case-2)
- [3. Invocations to the now-removed constructor via super()](#case-3)

<br>

### 1. Invocations to the now-removed constructor <a name="case-1"></a>
#### Example
The method `createTeam()` is defined within the `TeamFactory` class.
It invokes the now-removed constructor to create an instance of type `Team.`
Then, a broken use is reported pointing to the constructor invocation expression.

```java
public class TeamFactory {
  public Team createTeam(List<Person> members) {
    // Broken use reported here
    Team team = new Team(members);
    return team;
  }

}
```

---

### 2. Invocations to the now-removed constructor when creating anonymous classes <a name="case-2"></a>
#### Example
The method `createEmployee()` is defined within the `PeopleFactory` class.
It invokes the now-removed constructor to create an instance of type `Person.`
Then, a broken use is reported pointing to the constructor invocation expression.

```java
public class PeopleFactory {
  public Person createEmployee(String name, int age) {
    // Broken use reported here
    Person employee = new Person(name, age) {
      @Override
      public String display() {
        return null;
      }
    };
    return employee;
  }

}
```

---

### 3. Invocations to the now-removed constructor via super() <a name="case-3"></a>
#### Example
The class `Employee` in a client project extends the abstract class `Person`.
The constructor invokes the `Person` constructor via `super()`.
Then, a broken use is reported pointing to the constructor invocation expression.

```java
public class Employee extends Person {
  private String role;

  public Employee(String name, int age, String role) {
    // Broken use reported here
    super(name, age);
    this.role = role;
  }

  public String display() {
    System.out.println("Employee: " + name);
  }

}
```
