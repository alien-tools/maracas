---
layout: bc
title: Method Removed
---

## Description
A method is removed from its owning class.

## Broken Use
Hereafter, we list the broken uses that are currently detected by Maracas.

### 1. Invocations to the now-removed method
#### Example
Method `add()` is removed from the class `Math` in the library project.
Method `displayAddition()`—defined in the `Calculator` class in a client project—invokes the now-removed method.
Then, a broken use is reported pointing to the invocation statement.

**Old version of the library:**
```java
public class Math {
  public static int add(int num1, int num2) {
    return num1 + num2;
  }
}
```

**New version of the library:**
```java
public class Math {
  // Method add() has been removed
}
```

**Broken use in the client code:**
```java
public class Calculator {
  public void displayAddition(int num1, int num2) {
    // Broken use due to a method removed change
    int addition = Math.add(num1, num2);
    System.out.println(addition);
  }
}
```


### 2. Methods overriding the now-removed method
#### Example:
Method `power()` is removed from the interface `Math` in the library project.
The `MathImpl` class in a client project overrides the previous method.
Then, a broken use is reported pointing to the client method declaration.

**Old version of the library:**
```java
public interface Math {
  int power(int base, int power);
}
```

**New version of the library:**
```java
public interface Math {
  // Method power() has been removed
}
```

**Broken use in the client code:**
```java
public class MathImpl {
  // Broken use due to a method removed change
  @Override
  public void power(int base, int power) {
    int result = 1;
    for (int i = 0; i < power; i++)
      result = result * base;

    return result;
  }
}
```
