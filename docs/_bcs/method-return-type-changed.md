---
layout: bc
title: Method Return Type Changed
---

> The return type of a method has been changed.

---

## Example
Method `add()` is removed from the class `Math` in the library project.

```diff
public interface Math {
-int power(int base, int power);
-long factorial(int n);
+long power(int base, int power);
+int factorial(int n);

}
```

---

## Broken Uses
Hereafter, we list the **broken uses** that are currently detected by Maracas.

- [1. Invocations to the modified method](#case-1)
- [2. Methods overriding the modified method](#case-2)

<br>

### 1. Invocations to the modified method <a name="case-1"></a>
Invocations to the method in a statement where the expected type is not compatible with the new type.

#### Example
Method `displayPower()`—defined in the `Calculator` class in a client project—invokes an implementation of the `power()` method.
The `long` value is assigned to an `int` variable.
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


### 2. Methods overriding the modified method <a name="case-1"></a>
Methods overriding the modified method where the new type is not compatible with the client method type.

#### Example
The `MathImpl` class in a client project overrides the `factorial()` method.
However, there is a mismatch between the return types of both methods.
Then, a broken use is reported pointing to the client method declaration.

```java
public class MathImpl implements Math {
  // Broken use reported here
  @Override
  public long factorial(int n) {
    if (n == 0)
      return Long.valueOf(1);

    return Long.valueOf(n * factorial(n - 1));
  }

}
```
