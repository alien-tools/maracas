---
layout: bc
title: Class Now Final
---

> The `final` modifier is added to the declaration of a library class.

---

## Example
The `final` modifier is added to the declaration of the `Person` class in the library project.

```diff
-public class Person { }
+public final class Person { }
```

---

## Broken Uses
Hereafter, we list the **broken uses** that are currently detected by Maracas.

- [1. Regular and anonymous classes extending the now-final class](#case-1)

<br>

### 1. Regular and anonymous classes extending the now-final class <a name="case-1"></a>
#### Example
The `Employee` class—defined in a client project—extends the class `Person`.
Then, a broken use is reported pointing to the `Employee` class declaration as it cannot extend the `Person` class anymore.

```java
// Broken use reported here
public class Employee extends Person {
  
}
```
