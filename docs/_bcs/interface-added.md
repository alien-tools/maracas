---
layout: bc
title: Interface Added
---

> A class implements a new interface.

---

## Example
The class `LandVehicles` in the library project implements the interface `Vehicle`.

```java
public interface Vehicle {
  String getType();
}
```

```diff
-public abstract class LandVehicle { }
+public abstract class LandVehicle implements Vehicle { }
```

---

## Broken Uses
Hereafter, we list the **broken uses** that are currently detected by Maracas.   

- [1. Concrete classes extending the affected abstract type](#case-1)

<br>

### 1. Concrete classes extending the affected abstract type <a name="case-1"></a>

#### Example
The concrete `Car` class in a client project extends the `LandVehicle` abstract class defined in the library.
The `Car` does not override the `getType()` method declared within the now-added interface (i.e. `Vehicle`).
Then, a broken use is reported pointing to the class declaration.

```java
// Broken use reported here
public class Car extends LandVehicle { }
```
