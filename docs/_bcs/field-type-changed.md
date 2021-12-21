---
layout: bc
title: Field Type Changed
---

> The type of a field has been changed.

---

## Example
The type of the field `type` in the class `Vehicle` in the library project is changed from `String` to `int`.

```diff
public class Vehicle {
-  public String type;
+  public int type;

}
```
---

## Broken Uses
Hereafter, we list the **broken uses** that are currently detected by Maracas.   

- [1. Read accesses of the modified field](#case-1)
- [2. Assignments to the modified field](#case-2)

<br>

### 1. Read accesses of the modified field <a name="case-1"></a>
#### Example

The method `displayType()`—defined in the `VehicleInfo` class in a client project—accesses the `type` field of a `Vehicle` object and assigns its value to a variable of type `String`.
Then, a broken use is reported pointing to the variable assignment.

```java
public class VehicleInfo {
  public static void displayType(Vehicle vehicle) {
    // Broken use reported here
    String type = vehicle.type;
    System.out.println(type);
  }

}
```


### 2. Assignments to the modified field <a name="case-2"></a>
#### Example
The method `changeType()`—defined in the `VehicleInfo` class in a client project—assigns a new `String` value to the `type` field of a `Vehicle` object.
Then, a broken use is reported pointing to the field assignment.

```java
public class VehicleInfo {
  public static void changeType(Vehicle vehicle, String type) {
    // Broken use reported here
    vehicle.type = type;
  }

}
```
