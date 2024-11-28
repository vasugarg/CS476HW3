package org.cs476.hw3

import FuzzyExp._
import FuzzyMath._

object Main:

  private def defineClass(
                           name: String,
                           fields: List[Field],
                           methods: List[Method],
                           constructor: Constructor,
                           parent: Option[FuzzyExp] = None
                         ): ClassDef =
    val classDef = ClassDef(name, fields, methods, constructor, parent.collect { case p: ClassDef => p })
    classDef.eval()
    Public(name, fields.map(_.f_name), methods.map(_.m_name)).eval()
    classDef.asInstanceOf[ClassDef]

  private def instantiateClass(varName: String, className: String, args: Map[String, Value]): Unit =
    val instance = Instantiate(varName, className, args)
    instance.eval()

  private def invokeMethod(instanceVar: Var, methodName: String, arguments: Map[String, Value]): Unit =
    MethodInvocation(instanceVar, methodName, arguments).eval()

  private def defineAbstractClass(
                                   name: String,
                                   fields: List[Field],
                                   concreteMethods: List[Method],
                                   abstractMethods: List[AbstractMethod],
                                   constructor: Constructor
                                 ): AbstractClassDef =
    val abstractClassDef = AbstractClassDef(name, fields, concreteMethods, abstractMethods, constructor)
    abstractClassDef.eval()
    Public(name, fields.map(_.f_name), concreteMethods.map(_.m_name)).eval()
    abstractClassDef.asInstanceOf[AbstractClassDef]

  @main def runExample(): Unit =
    val pointClass = defineClass(
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
      )
    )

    instantiateClass("p", "Point", Map(
      "x_init" -> Value(Map("" -> 0.0)),
      "y_init" -> Value(Map("" -> 0.0))
    ))

    invokeMethod(Var("p"), "move", Map(
      "dx" -> Value(Map("" -> 1.0)),
      "dy" -> Value(Map("" -> 2.0))
    ))

    val fields = FuzzyMath.getInstanceFields("p", FuzzyMath.getGlobalScope)
    println(fields)

  @main def runInheritanceExample(): Unit =
    val baseClass = defineClass(
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
      )
    )

    val derivedClass = defineClass(
      name = "ColoredPoint",
      fields = List(Field("color")),
      methods = List(
        Method(
          m_name = "setColor",
          args = List(Assign("newColor", Var("newColor"))),
          exp = List(
            Assign("color", Var("newColor"))
          )
        )
      ),
      constructor = Constructor(
        exp = List(
          Assign("x", Var("x_init")),
          Assign("y", Var("y_init")),
          Assign("color", Var("color_init"))
        )
      ),
      parent = Some(baseClass)
    )

    instantiateClass("cp", "ColoredPoint", Map(
      "x_init" -> Value(Map("" -> 0.0)),
      "y_init" -> Value(Map("" -> 0.0)),
      "color_init" -> Value(Map("" -> 1.0))
    ))

    invokeMethod(Var("cp"), "move", Map(
      "dx" -> Value(Map("" -> 1.0)),
      "dy" -> Value(Map("" -> 2.0))
    ))

    invokeMethod(Var("cp"), "setColor", Map(
      "newColor" -> Value(Map("" -> 2.0))
    ))

    val fields = FuzzyMath.getInstanceFields("cp", FuzzyMath.getGlobalScope)
    println(s"Fields of 'cp': $fields")

  @main def runAbstractClassExample(): Unit =
    // Define an abstract class 'Shape' with an abstract method 'area'
    val shapeClass = defineAbstractClass(
      name = "Shape",
      fields = List(Field("x"), Field("y")),
      concreteMethods = List(),
      abstractMethods = List(
        AbstractMethod("area")
      ),
      constructor = Constructor(
        exp = List(
          Assign("x", Var("x_init")),
          Assign("y", Var("y_init"))
        )
      )
    )

    // Define a concrete class 'Circle' that extends 'Shape' and implements 'area'
    val circleClass = defineClass(
      name = "Circle",
      fields = List(Field("radius")),
      methods = List(
        Method(
          m_name = "area",
          args = List(),
          exp = List(
            Assign("area", Multiplication(Value(Map("pi" -> 3.14159)), Multiplication(Var("radius"), Var("radius"))))
          )
        )
      ),
      constructor = Constructor(
        exp = List(
          Assign("x", Var("x_init")),
          Assign("y", Var("y_init")),
          Assign("radius", Var("radius_init"))
        )
      ),
      parent = Some(shapeClass) // No casting needed here
    )

    // Instantiate a 'Circle' object
    instantiateClass("c", "Circle", Map(
      "x_init" -> Value(Map("" -> 0.0)),
      "y_init" -> Value(Map("" -> 0.0)),
      "radius_init" -> Value(Map("" -> 5.0))
    ))

    // Invoke the 'area' method on the instance 'c'
    invokeMethod(Var("c"), "area", Map())

    // Access and print the updated fields
    val fields = FuzzyMath.getInstanceFields("c", FuzzyMath.getGlobalScope)
    println(s"Fields of 'c': $fields")

@main def runPartialEvaluationExample(): Unit =
  // Define a variable 'var' in the global scope with an undefined value
  // getGlobalScope.setVariable("var", Var("var")) // 'var' is uninitialized

  // Define an expression that includes 'var'
  val expr = Multiplication(
    Value(Map("x" -> 3.0)),
    Multiplication(
      Addition(Value(Map("x" -> 5.0)), Value(Map("x" -> 1.0))),
      Var("var")
    )
  )

  // Evaluate the expression (partial evaluation)
  val result = expr.eval()

  // Print the result
  println(s"Result of partial evaluation:\n$result\n")

  // Now, assign a value to 'var' and evaluate again
  getGlobalScope.setVariable("var", Value(Map("x" -> 2.0)))

  // Evaluate the expression again
  val fullResult = expr.eval()

  // Print the fully evaluated result
  println(s"Result after assigning value to 'var':\n$fullResult\n")

  // **Demonstrate conditional constructs with partial evaluation**
  // Reset 'var' to an undefined state
  //getGlobalScope.setVariable("var", Var("var"))

  // Define a conditional expression using IFTRUE
  val conditionalExpr = IFTRUE(
    GREATEREQUAL(
      Multiplication(Value(Map("x" -> 15.0)), Var("var")),
      Addition(Value(Map("x" -> 2.0)), Var("var1"))
    ),
    Assign("somevar", Addition(Var("var"), Value(Map("x" -> 3.0)))),
    Assign("somevar", Value(Map("x" -> 0.0)))
  )

  // Evaluate the conditional expression
  val conditionalResult = conditionalExpr.eval()

  // Print the result of partial evaluation
  println(s"Result of conditional partial evaluation:\n$conditionalResult\n")

  // Now, assign values to 'var' and 'var1'
  getGlobalScope.setVariable("var", Value(Map("x" -> 1.0)))
  getGlobalScope.setVariable("var1", Value(Map("x" -> 10.0)))

  // Evaluate the conditional expression again
  val conditionalFullResult = conditionalExpr.eval()

  // Print the fully evaluated result
  println(s"Result after assigning values to 'var' and 'var1':\n$conditionalFullResult\n")

  // Access and print the value of 'somevar' from the global scope
  val somevarValue = getGlobalScope.searchBinding("somevar")
  println(s"Value of 'somevar' in global scope: $somevarValue")

@main def runMultiplicationAssociativityTest(): Unit =
  // Define the expression: Multiply(Value(3), Multiply(Value(5), Var("var")))
  val expr = Multiplication(
    Value(Map("value" -> 3.0)),
    Multiplication(
      Value(Map("value" -> 5.0)),
      Var("var")
    )
  )

  // Evaluate the expression (partial evaluation)
  val result = expr.eval()
  println(s"Result of partial evaluation:\n$result\n")

  // Expected result after applying associativity:
  // Multiply(Value(15), Var("var"))

  // Now, assign a value to 'var' and evaluate again
  getGlobalScope.setVariable("var", Value(Map("value" -> 2.0)))

  // Evaluate the expression again
  val fullResult = expr.eval()
  println(s"Result after assigning value to 'var':\n$fullResult\n")
  // Expected full result: Value(Map("value" -> 30.0))
