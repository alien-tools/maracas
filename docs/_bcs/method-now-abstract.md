---
layout: bc
title: Method Now Abstract
---

> The `abstract` modifier is added to the declaration of a library method and its body is removed.

---

## Example
The abstract class `Vehicle` removes the `static` modifier and adds the `abstract` modifier to the `move` method.
Afterwards, the method's body is removed.

```diff
public abstract class Vehicle {
-  public static void move() {
-    System.out.println("The vehicle is moving.");
-  }
+  public abstract move();
}
```

---

## Broken Uses
Hereafter, we list the **broken uses** that are currently detected by Maracas.

- [1. Non-abstract types extending/implementing the enclosing type of the now-abstract method](#case-1)
- [2. Invocations in subtypes of the now-abstract method](#case-2)

<br>

### 1. Non-abstract types extending/implementing the enclosing type of the now-abstract method <a name="case-1"></a>
(Unless the now-abstract method is already implemented somewhere in the hierarchy)
#### Example
The `LanVehicle` class—declared in the in a client project—extends the `Vehicle` class form the library.
Then, a broken use is reported pointing to the class declaration given that no implementation for the now-abstract method `move` has been provided.

```java
public class LandVehicle extends Vehicle {

}
```


### 2. Invocations in subtypes of the now-abstract method <a name="case-1"></a>
#### Example
The method `printMove`—defined in the `LandVehicle` class in a client project—invokes the now-abstract method `move` from the `Vehicle` class.
Then, a broken use is reported pointing to the method invocation expression.

```java
public class LandVehicle extends Vehicle {

  public static void printMove() {
    // Broken use reported here
    Vehicle.move();
  }

}
```
