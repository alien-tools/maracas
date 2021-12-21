---
layout: bc
title: Method Added to Interface
---

> A new method is added to an interface.

---

## Example
The interface `Vehicle` in the library project adds the method `getColor()`.

```diff
public interface Vehicle {
  String getType();
+  String getColor();

}

```


## Broken Uses
Hereafter, we list the **broken uses** that are currently detected by Maracas.   

- [1. Concrete classes implementing the modified interface](#case-1)

<br>

### 1. Concrete classes implementing the modified interface <a name="case-1"></a>

#### Example
The concrete `Bus` class in a client project implements the `Vehicle` interface defined in the library.
The `Bus` car does not override the `getColor()` method declared within the modified interface (i.e. `Vehicle`).
Then, a broken use is reported pointing to the class declaration.

```java
// Broken use reported here
public class Bus implements Vehicle {
  @Override
  public String getType() {
    return "Bus";
  }
}
```
