---
layout: bc
title: Interface Removed
---

> A class removes one of its interfaces.

---

## Example
The class `LandVehicles` in the library project removes the interface `Vehicle` from its declaration.

```java
public interface Vehicle {
  public static final int INITIAL_KMS = 0;

  String getType();

  default void move() {
    System.out.println("The vehicle is moving.");
  }

}
```

```diff
public abstract class LandVehicles
- implements Vehicle {
+ {

}
```

---

## Broken Uses
Hereafter, we list the **broken uses** that are currently detected by Maracas.   

- [1. Methods overriding methods declared in the now-removed interface](#case-1)
- [2. Accesses of fields declared in the now-removed interface](#case-2)
- [3. Invocations of static or default methods declared in the now-removed interface](#case-3)
- [4. Casts of subtypes referencing the now-removed interface](#case-4)

<br>

### 1. Methods overriding methods declared in the now-removed interface <a name="case-1"></a>

#### Example
The concrete `Car` class in a client project extends the `LandVehicle` abstract class defined in the library.
The `Car` overrides the `getType()` method declared within the now-removed interface (i.e. `Vehicle`).
Then, a broken use is reported pointing to the client method declaration.

```java
public class Car extends LandVehicle {
  private String type;

  // Broken use reported here
  @Override
  public String getType() {
    return this.type;
  }

}
```

---

### 2. Accesses of fields declared in the now-removed interface <a name="case-2"></a>

#### Example
The concrete `Car` class in a client project extends the `LandVehicle` abstract class defined in the library.
The `resetKms()` method is defined within the `Car` class and it accesses the `INITIAL_KMS` constant defined in the `Vehicle` interface.
Then, a broken use is reported pointing to the line of the field access expression.

```java
public class Car extends LandVehicle {
  public void resetKms() {
    // Broken use reported here
    this.kms = LandVehicle.INITIAL_KMS;
  }

}
```

---

### 3. Invocations of static or default methods declared in the now-removed interface <a name="case-3"></a>

#### Example
The concrete `Car` class in a client project extends the `LandVehicle` abstract class defined in the library.
The `moveCar()` method is defined within the `Car` class and it invokes the `move()` method defined in the `Vehicle` interface.
Then, a broken use is reported pointing to the line of the method invocation expression.

```java
public class Car extends LandVehicle {
  public void moveCar() {
    // Broken use reported here
    move();
    System.out.println("It is a car!");
  }

}
```

---

### 4. Casts of subtypes referencing the now-removed interface <a name="case-4"></a>

#### Example
On the one hand, the concrete `Car` class in a client project extends the `LandVehicle` abstract class defined in the library.
On the other hand, the `VehicleFactory` class within the same client project defines the `createCar()` method.
Such method creates a `Car` instance and casts it to a `Vehicle` object.
Then, a broken use is reported pointing to the cast expression.

```java
public class Car extends LandVehicle { }

public class VehicleFactory {
  public Vehicle createCar() {
    // Broken use reported here
    Vehicle vehicle = (Vehicle) new Car();
    return vehicle;
  }
  
}
```
