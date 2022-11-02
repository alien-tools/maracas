---
layout: bc
title: Superclass Added
---

> A class adds a new superclass.

---

## Example
The class `LandVehicles` in the library project adds the superclass `Vehicle` to its declaration.

```java
public abstract class Vehicle {
  public static final int INITIAL_KMS = 0;

  String getType();

  public static void move() {
    System.out.println("The vehicle is moving.");
  }

}
```

```diff
+public abstract class LandVehicles {
-public abstract class LandVehicles extends Vehicle {

}
```

---

## Broken Uses
Hereafter, we list the **broken uses** that are currently detected by Maracas.   

- [1. Unimplemented abstract methods](#case-1)

<br>

### 1. Unimplemented abstract methods <a name="case-1"></a>

#### Example
The concrete `Car` class in a client project extends the `LandVehicle` abstract class defined in the library.
The `Car` class does not provide an implementation for the `move` method defined in the `Vehicle` abstract class.\
Then, a broken use is reported pointing to the `Car` class declaration.

```java
// Broken use reported here
public class Car extends LandVehicle {
  private String type;

}
```
