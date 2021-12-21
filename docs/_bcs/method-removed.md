---
layout: bc
title: Method Removed
---

> A method is removed from its owning class.

---

## Example
Method `add()` is removed from the class `Math` in the library project.

```diff
public interface Math {
- int power(int base, int power);

}
```

---

## Broken Uses
Hereafter, we list the **broken uses** that are currently detected by Maracas.

- [1. Invocations to the now-removed method](#case-1)
- [2. Methods overriding the now-removed method](#case-2)

<br>

### 1. Invocations to the now-removed method <a name="case-1"></a>
#### Example
Method `displayPower()`—defined in the `Calculator` class in a client project—invokes an implementation of the now-removed method.
Then, a broken use is reported pointing to the method invocation expression.

```java
public class Calculator {
  public void displayPower(int base, int power) {
    Math math = new MathImpl();
    // Broken use reported here
    int result = math.power(base, power);
    System.out.println(result);
  }

}
```


### 2. Methods overriding the now-removed method <a name="case-1"></a>
#### Example
The `MathImpl` class in a client project overrides the now-removed method.
Then, a broken use is reported pointing to the client method declaration.

```java
public class MathImpl implements Math {
  // Broken use reported here
  @Override
  public int power(int base, int power) {
    int result = 1;
    for (int i = 0; i < power; i++)
      result = result * base;

    return result;
  }

}
```
