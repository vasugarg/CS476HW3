Certainly! I'll update your `README.md` to include the additional changes discussed earlier and provide detailed explanations for each new feature and modification. The updated README will reflect the latest features and improvements in your FuzzyLang project.

---

# FuzzyLang

## Overview

FuzzyLang is a domain-specific language designed to support both fuzzy logic operations and object-oriented programming constructs. The project offers a robust framework for designers working with logic gates, fuzzy set operations, and object-oriented principles like encapsulation, inheritance, and polymorphism. With FuzzyLang, developers can define classes, abstract classes, and methods while performing fuzzy set operations, making it ideal for complex scenarios that require both logical computations and structured code organization.

## Key Features

### 1. Fuzzy Logic Operations

- **Fuzzy Sets**: Support for sets where elements have varying degrees of membership, allowing non-binary (gradual) set operations.
- **Operations Supported**:
    - **Union**: Combines two fuzzy sets by taking the maximum membership value for each element.
    - **Intersection**: Merges sets by taking the minimum membership value for each element.
    - **Addition**: Adds membership values of two sets element-wise, capped at a maximum value of 1.
    - **Multiplication**: Multiplies membership values of two sets element-wise.
- **Complement and XOR**:
    - **Complement**: Inverts the membership value of each element in the fuzzy set.
    - **XOR**: Performs symmetric difference by taking the absolute difference of membership values.

### 2. Object-Oriented Programming Constructs

- **Class and Abstract Class Definitions**:
    - **Concrete Classes**: Define fully implemented classes with fields and methods.
    - **Abstract Classes**: Define classes with unimplemented (abstract) methods that subclasses must implement.
- **Inheritance**: Allows classes to inherit fields and methods from parent classes, enabling polymorphism and code reuse.
- **Access Modifiers**:
    - **Public**: Members accessible from any scope.
    - **Private**: Members only accessible within the class.
    - **Protected**: Members accessible within the class and subclasses.
- **Instance Creation**:
    - Supports instantiation of concrete classes.
    - Prevents instantiation of abstract classes, enforcing the requirement that subclasses must implement abstract methods.
- **Method Invocation**:
    - Supports dynamic method invocation on instances.
    - Handles method overriding and inheritance correctly.

### 3. Macros for Reusability

- **Macro Definition**: Allows creation of reusable code snippets that can accept parameters and perform complex operations.
- **Macro Invocation**: Enables the invocation of defined macros with specific arguments, promoting code reuse and reducing repetition.

### 4. Scoped Execution and Blocks

- **Scope**: Isolates variables within a specific execution context, preventing conflicts with outer scope variables.
- **Block Constructs**: Group operations or expressions together, ensuring variables within the block are independent of the outer scope.

### 5. Dynamic Method Invocation and Instance Handling

- **Instance References**: Introduces `InstanceRef` to handle instances within the evaluator consistently.
- **Virtual Dispatch Table**: Manages method overriding for inherited classes, ensuring correct method execution in cases of polymorphism.

### 6. Testing and Global Scope Management

- **Isolated Testing Environment**: Resets the global scope and registries before each test to prevent interference between tests.
- **Helper Methods**: Provides utility functions to access the global scope and instance fields within tests.

### 7. Logging

- **Integrated Logging**: Uses SLF4J for logging variable creation, scope entry/exit, and operation execution.

---

## Prerequisites

- **Scala 3.x**: This project uses Scala 3 features.
- **sbt (Scala Build Tool)**: For building the project and running tests.
- **Java Development Kit (JDK) 8, 11, or higher**: Required to run Scala applications.

## Getting Started

### Installation

1. **Clone the Repository**

   ```bash
   git clone https://github.com/vasugarg/CS476HW2.git
   ```

2. **Navigate to the Project Directory**

   ```bash
   cd CS476HW2
   ```

3. **Compile the Project**

   ```bash
   sbt compile
   ```

4. **Run Tests**

   ```bash
   sbt test
   ```

---

## FuzzyLang Constructs

FuzzyLang offers a comprehensive set of constructs for logic gate designers and object-oriented programming. Below is the complete list of constructs available in FuzzyLang:

<details>
<summary>Click to expand list of FuzzyLang Constructs</summary>

- [Value](#value)
- [Var](#var)
- [Assign](#assign)
- [Scope](#scope)
- [Block](#block)
- [Union](#union)
- [Intersection](#intersection)
- [Addition](#addition)
- [Multiplication](#multiplication)
- [Complement](#complement)
- [XOR](#xor)
- [MacroDef](#macrodef)
- [MacroInvoke](#macroinvoke)
- [AbstractClassDef](#abstractclassdef)
- [ClassDef](#classdef)
- [Public](#public)
- [Private](#private)
- [Protected](#protected)
- [Instantiate](#instantiate)
- [MethodInvocation](#methodinvocation)
- [InstanceRef](#instanceref)
- [Conditional Constructs](#conditional-constructs)
- [Comparison Operators](#comparison-operators)
- [Logical Operators](#logical-operators)

</details>

---

### Value

`Value` represents a fuzzy set, which is a map of elements to their membership values. This construct can directly hold values of elements and is used for creating and manipulating fuzzy sets.

Example:

```scala
val setA = Value(Map("a" -> 0.7, "b" -> 0.5, "c" -> 0.3))
```

### Var

The `Var` construct is used to reference a variable within a scope. It allows you to retrieve values assigned to variables in expressions.

Example:

```scala
val variable = Var("someVariable")
```

### Assign

`Assign` assigns the evaluated result of an expression to a variable. This is essential for modifying variable values within a scope.

Example:

```scala
Assign("x", Value(Map("a" -> 0.5)))
```

### Scope

`Scope` defines a new scope, an isolated environment where variables and operations are independent of other scopes. This is particularly useful for nested expressions and ensuring no interference between variables in different scopes.

Example:

```scala
Scope("innerScope", Block(List(Assign("y", Value(Map("b" -> 0.6))))))
```

### Block

A `Block` is a sequence of expressions executed in the same scope. It allows grouping of multiple expressions, which are evaluated in order.

Example:

```scala
Block(List(
  Assign("x", Value(Map("a" -> 0.5))),
  Assign("y", Value(Map("b" -> 0.8)))
))
```

### Union

Performs a union operation between two fuzzy sets, selecting the maximum membership value for each element. This is the `OR` operation in fuzzy logic.

Example:

```scala
val setA = Value(Map("a" -> 0.7, "b" -> 0.5))
val setB = Value(Map("b" -> 0.6, "c" -> 0.8))
val unionExp = Union(setA, setB)
```

### Intersection

Computes the intersection of two fuzzy sets, using the minimum membership value for each element.

Example:

```scala
val setA = Value(Map("a" -> 0.7, "b" -> 0.5))
val setB = Value(Map("b" -> 0.6, "c" -> 0.8))
val intersectionExp = Intersection(setA, setB)
```

### Addition

Adds two fuzzy sets by combining values element-wise, with the result capped at a maximum value of 1.0.

Example:

```scala
val setA = Value(Map("a" -> 0.7, "b" -> 0.5))
val setB = Value(Map("b" -> 0.6, "c" -> 0.8))
val additionExp = Addition(setA, setB)
```

### Multiplication

Multiplies two fuzzy sets by combining values element-wise.

Example:

```scala
val setA = Value(Map("a" -> 0.7, "b" -> 0.5))
val setB = Value(Map("b" -> 0.6, "c" -> 0.8))
val multiplicationExp = Multiplication(setA, setB)
```

### Complement

Calculates the complement of a fuzzy set, where each membership value is subtracted from 1.

Example:

```scala
val set = Value(Map("a" -> 0.7, "b" -> 0.5))
val complementExp = Complement(set)
```

### XOR

Calculates the symmetric difference (exclusive OR) between two fuzzy sets.

Example:

```scala
val setA = Value(Map("a" -> 0.7, "b" -> 0.5))
val setB = Value(Map("b" -> 0.6, "c" -> 0.8))
val xorExp = XOR(setA, setB)
```

### MacroDef

Defines a reusable macro with a name, parameters, and body, allowing repetitive operations to be encapsulated.

Example:

```scala
val scaleMacro = MacroDef(
  name = "scale",
  params = List("factor", "input"),
  body = Block(List(
    Assign("scaled_a", Multiplication(Value(Map("a" -> 0.5)), Var("factor"))),
    Assign("scaled_b", Multiplication(Value(Map("b" -> 0.8)), Var("factor"))),
    Union(Var("scaled_a"), Var("scaled_b"))
  ))
)
```

### MacroInvoke

Invokes a previously defined macro with specified arguments. This runs the macro’s body using the passed values for each parameter.

Example:

```scala
MacroInvoke("scale", List(Value(Map("" -> 2.0)), Value(Map("a" -> 0.5, "b" -> 0.8))))
```

### AbstractClassDef

Defines an abstract class, allowing you to specify both abstract and concrete methods. Abstract methods are method signatures without implementations, which must be implemented by subclasses.

Example:

```scala
val shapeClass = AbstractClassDef(
  name = "Shape",
  fields = List(Field("x"), Field("y")),
  concreteMethods = List(),
  abstractMethods = List(AbstractMethod("area")),
  constructor = Constructor(
    exp = List(
      Assign("x", Var("x_init")),
      Assign("y", Var("y_init"))
    )
  )
)
```

### ClassDef

Defines a concrete class with fields, methods, a constructor, and optional inheritance from other classes. Supports object-oriented features like encapsulation and inheritance.

Example:

```scala
val pointClass = ClassDef(
  name = "Point",
  fields = List(Field("x"), Field("y")),
  methods = List(
    Method(
      m_name = "move",
      args = List(Assign("dx", Var("dx")), Assign("dy", Var("dy"))),
      exp = List(
        Assign("x", Addition(Var("x"), Var("dx"))),
        Assign("y", Addition(Var("y"), Var("dy")))
      )
    )
  ),
  constructor = Constructor(
    exp = List(
      Assign("x", Var("x_init")),
      Assign("y", Var("y_init"))
    )
  ),
  parent = None
)
```

### Public, Private, and Protected

Control access to fields and methods in classes, allowing encapsulation of functionality. These modifiers restrict the visibility and accessibility of class members.

Example:

```scala
Public("Point", List("x", "y"), List("move"))
Private("Point", List("x"), List("move"))
Protected("Point", List("y"), List("move"))
```

### Instantiate

Creates an instance of a concrete class, binding it to a variable in the global scope. An error is raised if instantiation of an abstract class is attempted.

Example:

```scala
Instantiate("p", "Point", Map(
  "x_init" -> Value(Map("" -> 0.0)),
  "y_init" -> Value(Map("" -> 0.0))
))
```

### MethodInvocation

Invokes a method on an instance, passing arguments as needed.

Example:

```scala
MethodInvocation(Var("p"), "move", Map(
  "dx" -> Value(Map("" -> 1.0)),
  "dy" -> Value(Map("" -> 2.0))
))
```

### InstanceRef

`InstanceRef` is a construct used internally by the evaluator to represent references to instances. It ensures that instance variables are handled consistently throughout the evaluation process.

**Usage in Evaluator:**

When a class is instantiated, the variable holding the instance is bound to an `InstanceRef` containing the instance name. This allows methods and fields to be accessed correctly.

Example:

```scala
case class InstanceRef(name: String) extends FuzzyExp
```

### Conditional Constructs

Supports conditional execution using constructs like `IFTRUE`, allowing for control flow based on evaluated conditions.

Example:

```scala
IFTRUE(
  GREATEREQUAL(Var("score"), Value(Map("" -> 60.0))),
  Assign("result", Value(Map("" -> "Pass"))),
  Assign("result", Value(Map("" -> "Fail")))
)
```

### Comparison Operators

Allows for comparison between expressions using operators like `GREATEREQUAL`, `LESSERTHAN`, etc.

Example:

```scala
GREATEREQUAL(Value(Map("" -> 5.0)), Var("threshold"))
```

### Logical Operators

Supports logical operations such as `AND`, `OR`, and `NOT` for combining conditions.

Example:

```scala
AND(
  GREATEREQUAL(Var("score"), Value(Map("" -> 50.0))),
  LESSERTHAN(Var("score"), Value(Map("" -> 75.0)))
)
```

---

## Detailed Explanations of Recent Additions

### 1. Handling of `InstanceRef` in Evaluator

**Problem Addressed:**

In the evaluator, instance variables were causing type mismatch errors because they were sometimes bound directly to strings (instance names) instead of expressions of type `FuzzyExp`. This led to issues when evaluating expressions involving instances.

**Solution:**

- Introduced the `InstanceRef` class extending `FuzzyExp` to represent instance references.
- Adjusted the `evalInScope` method to handle `InstanceRef` appropriately.
- Ensured that instance variables are consistently represented as `InstanceRef` within the evaluator.

**Example Implementation:**

```scala
sealed trait FuzzyExp
case class InstanceRef(name: String) extends FuzzyExp
```

**Benefits:**

- Maintains type safety by ensuring all expressions are of type `FuzzyExp`.
- Simplifies the evaluator logic by handling instances uniformly.

---


### 2. Implementation of Partial Evaluation

**Objective:**

To enable the evaluator to partially evaluate expressions when not all variable values are known, providing as much simplification as possible without complete information.

**Approach:**

- **Expression Simplification:** Implemented evaluation rules for each operation to simplify expressions based on the available information.
- **Handling Undefined Variables:** When a variable's value is not known (undefined), the evaluator retains the variable in the expression rather than throwing an error.
- **Associativity and Commutativity:** Utilized the associative and commutative properties of operations like addition and multiplication to reorder and combine terms.

**Example Implementation:**

```scala
case Addition(lhs, rhs) =>
  val leftEval = lhs.evalInScope(currentScope)
  val rightEval = rhs.evalInScope(currentScope)
  (leftEval, rightEval) match {
    case (Value(lv), Value(rv)) =>
      Value(mergeSets(lv, rv, _ + _, cap = 1.0))
    case (Value(lv), _) =>
      Addition(Value(lv), rightEval)
    case (_, Value(rv)) =>
      Addition(leftEval, Value(rv))
    case _ =>
      Addition(leftEval, rightEval)
  }
```

**Benefits:**

- **Improved Flexibility:** Allows for expressions to be evaluated as much as possible, which is particularly useful in scenarios where some data is computed incrementally.
- **Error Avoidance:** Prevents the evaluator from failing due to undefined variables, enhancing robustness.

**Use Case in Tests:**

```scala
val expr = Multiplication(
  Value(Map("x" -> 3.0)),
  Multiplication(
    Addition(Value(Map("x" -> 5.0)), Value(Map("x" -> 1.0))),
    Var("var")
  )
)

val result = expr.eval()
// result: Multiplication(Value(Map("x" -> 18.0)), Var("var"))
```

After assigning a value to `var`:

```scala
getGlobalScope.setVariable("var", Value(Map("x" -> 2.0)))
val fullResult = expr.eval()
// fullResult: Value(Map("x" -> 36.0))
```

---

### 3. Improved Method Invocation Handling

**Enhancements:**

- Adjusted the evaluator to handle method invocations on instances more robustly.
- Added cases in the evaluator to correctly match and process `MethodInvocation` expressions involving `InstanceRef`.

**Example Implementation in Evaluator:**

    ```scala
    case MethodInvocation(instanceVar, methodName, arguments) =>
      val instanceNameExp = instanceVar.evalInScope(currentScope)
      val instanceName = instanceNameExp match {
        case InstanceRef(name) => name
        case _ =>
          throw new Exception(s"Invalid instance variable '$instanceVar', expected an instance reference")
      }
      // Proceed with method invocation
    ```

**Benefits:**

- Ensures that methods are invoked on the correct instances.
- Properly handles scope and variable bindings within methods.

---

### 4. Use of `virtualDispatchTable` in Method Invocation

**Purpose:**

To manage method overriding and inheritance in classes, ensuring that the correct method implementations are invoked based on the instance's class hierarchy.

**Implementation:**

- **Virtual Dispatch Table (VDT):** A mapping from method names to method implementations for each class.
- **Class Registry Updates:** When a class is defined, its VDT is populated with its methods and any inherited methods from parent classes.
- **Method Lookup:** During method invocation, the evaluator uses the VDT to find the appropriate method to execute.

**Example Implementation in Evaluator:**

```scala
def buildVirtualDispatchTable(classDef: ClassDef): Map[String, Method] = {
  val parentVDT = classDef.parent match {
    case Some(parentClassDef) => buildVirtualDispatchTable(parentClassDef)
    case None => Map.empty[String, Method]
  }
  parentVDT ++ classDef.methods.map(m => m.m_name -> m).toMap
}
```

**Method Invocation Process:**

1. **Retrieve Instance's Class VDT:**
    - Use the instance's class name to get its VDT from the class registry.
2. **Locate Method Implementation:**
    - Look up the method name in the VDT.
3. **Execute Method:**
    - Evaluate the method's expressions in a new scope, with proper argument bindings.

**Benefits:**

- **Correct Method Resolution:** Ensures that overridden methods in subclasses are invoked instead of parent class methods.
- **Supports Polymorphism:** Allows instances to be treated as instances of their parent classes while still invoking the correct subclass methods.
- **Simplifies Evaluator Logic:** Centralizes method resolution, making the evaluator code cleaner and more maintainable.

**Example Usage:**

```scala
// Assuming 'Circle' overrides the 'area' method from 'Shape'
MethodInvocation(Var("c"), "area", Map()).eval()
```

**Evaluator Steps:**

- Retrieves the VDT for the 'Circle' class.
- Finds the 'area' method in the VDT.
- Executes the 'Circle' class's implementation of 'area'.

---

## Logging

The project uses SLF4J for logging, providing insights into:

- Variable creation and updates.
- Scope entry and exit.
- Method invocations and instance creations.
- Error messages and exceptions.

**Example Log Output:**

```
INFO org.cs476.hw3.FuzzyMath$ -- Defined class 'Point'
INFO org.cs476.hw3.FuzzyScope -- Created binding for variable 'x' in scope 'Point_constructor' with value: null
INFO org.cs476.hw3.FuzzyScope -- Assigned Variable('x') in scope 'Point_constructor' with value: Value(Map("" -> 0.0))
INFO org.cs476.hw3.FuzzyMath$ -- Created instance 'Point_instance_0' of class 'Point'
```

---

## Conclusion

FuzzyLang provides a powerful set of features for users looking to work with both fuzzy logic and structured object-oriented designs. With its constructs for class and method definitions, abstract classes, inheritance, and fuzzy set operations, FuzzyLang is well-suited for applications requiring both logical precision and organized, extensible code structure.

---

## Future Work

- **Thread Safety**: Enhance the evaluator to be thread-safe for concurrent execution.
- **Additional Fuzzy Operations**: Implement more complex fuzzy logic operations as needed.
- **Improved Error Handling**: Provide more detailed error messages and recovery options.
- **Performance Optimization**: Optimize the evaluator for better performance with large fuzzy sets.

---

If you have any questions or need further assistance with FuzzyLang, please feel free to reach out!