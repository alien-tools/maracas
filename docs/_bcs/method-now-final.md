---
layout: bc
title: Method Now Final
---

> The `final` modifier is added to the declaration of a library method.

---

## Example
The abstract class `Vehicle` adds the `final` modifier to the `move` method.
Afterwards, the method's body is removed.

```diff
public abstract class Vehicle {
-  public void move() {
+  public final move() {
    System.out.println("The vehicle is moving.");
  }

}
```

---

## Broken Uses
Hereafter, we list the **broken uses** that are currently detected by Maracas.

- [1. Methods overriding the now-final method](#case-1)

<br>

### 1. Methods overriding the now-final method <a name="case-1"></a>
(With or without the explicit use of the `@Override` annotation)
#### Example
The `LanVehicle` class—declared in the in a client project—extends the `Vehicle` class form the library.
It overrides the `move` method coming from the `Vehicle` class.
Then, a broken use is reported pointing to the method declaration given that the method cannot be overriden anymore.

```java
public class LandVehicle extends Vehicle {
  // Broken use reported here
  @Override
  public final move() {
    System.out.println("The land vehicle is moving.");
  }
}
```
