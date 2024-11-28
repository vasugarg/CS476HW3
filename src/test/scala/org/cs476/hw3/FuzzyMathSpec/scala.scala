package org.cs476.hw3

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfterEach
import FuzzyExp._
import FuzzyMath._
import scala.collection.mutable

class FuzzyMathSpec extends AnyFlatSpec with Matchers with BeforeAndAfterEach {

  // Reset the global scope and registries before each test
  override def beforeEach(): Unit = {
    super.beforeEach()
    FuzzyMath.resetGlobalScope()
  }

  "Partial evaluation" should "simplify expressions with undefined variables" in {
    // Expression: Multiply(Value(3), Multiply(Add(Value(5), Value(1)), Var("var")))
    val expr = Multiplication(
      Value(Map("x" -> 3.0)),
      Multiplication(
        Addition(Value(Map("x" -> 5.0)), Value(Map("x" -> 1.0))),
        Var("var")
      )
    )

    val result = expr.eval()
    result shouldEqual Multiplication(Value(Map("x" -> 18.0)), Var("var"))
  }

  it should "fully evaluate expressions after variables are assigned" in {
    // Assign 'var' to a value
    getGlobalScope.setVariable("var", Value(Map("x" -> 2.0)))

    // Re-evaluate the previous expression
    val expr = Multiplication(
      Value(Map("x" -> 3.0)),
      Multiplication(Value(Map("x" -> 6.0)), Var("var"))
    )

    val result = expr.eval()
    result shouldEqual Value(Map("x" -> 36.0))
  }

  "Associativity in Multiplication" should "simplify nested multiplications" in {
    // Expression: Multiply(Value(3), Multiply(Value(5), Var("var")))
    val expr = Multiplication(
      Value(Map("value" -> 3.0)),
      Multiplication(
        Value(Map("value" -> 5.0)),
        Var("var")
      )
    )

    val result = expr.eval()
    result shouldEqual Multiplication(Value(Map("value" -> 15.0)), Var("var"))

    // Assign 'var' and fully evaluate
    getGlobalScope.setVariable("var", Value(Map("value" -> 2.0)))
    val fullResult = expr.eval()
    fullResult shouldEqual Value(Map("value" -> 30.0))
  }

  "Associativity in Addition" should "simplify nested additions" in {
    // Expression: Add(Value(2), Add(Value(3), Var("x")))
    val expr = Addition(
      Value(Map("value" -> 2.0)),
      Addition(
        Value(Map("value" -> 3.0)),
        Var("x")
      )
    )

    val result = expr.eval()
    result shouldEqual Addition(Value(Map("value" -> 5.0)), Var("x"))

    // Assign 'x' and fully evaluate
    getGlobalScope.setVariable("x", Value(Map("value" -> 4.0)))
    val fullResult = expr.eval()
    fullResult shouldEqual Value(Map("value" -> 9.0))
  }

  "Conditional constructs" should "partially evaluate when condition is undefined" in {
    // Expression using IFTRUE
    val expr = IFTRUE(
      GREATEREQUAL(
        Multiplication(Value(Map("x" -> 15.0)), Var("var")),
        Addition(Value(Map("x" -> 2.0)), Var("var1"))
      ),
      Assign("somevar", Addition(Var("var"), Value(Map("x" -> 3.0)))),
      Assign("somevar", Value(Map("x" -> 0.0)))
    )

    val result = expr.eval()
    result shouldEqual IFTRUE(
      GREATEREQUAL(
        Multiplication(Value(Map("x" -> 15.0)), Var("var")),
        Addition(Value(Map("x" -> 2.0)), Var("var1"))
      ),
      Addition(Var("var"), Value(Map("x" -> 3.0))),
      Value(Map("x" -> 0.0))
    )
  }

  it should "fully evaluate when condition becomes defined" in {
    // Assign values to 'var' and 'var1' to make the condition false
    getGlobalScope.setVariable("var", Value(Map("x" -> 0.5)))
    getGlobalScope.setVariable("var1", Value(Map("x" -> 10.0)))

    // Re-evaluate the previous expression
    val expr = IFTRUE(
      GREATEREQUAL(
        Multiplication(Value(Map("x" -> 15.0)), Var("var")),
        Addition(Value(Map("x" -> 2.0)), Var("var1"))
      ),
      Assign("somevar", Addition(Var("var"), Value(Map("x" -> 3.0)))),
      Assign("somevar", Value(Map("x" -> 0.0)))
    )

    val result = expr.eval()
    result shouldEqual Value(Map("x" -> 0.0))

    // Verify that 'somevar' has the correct value in the global scope
    val somevarValue = getGlobalScope.searchBinding("somevar")
    somevarValue shouldEqual Some(Value(Map("x" -> 0.0)))
  }

  "Comparison operators" should "correctly evaluate GREATEREQUAL" in {
    val expr = GREATEREQUAL(
      Value(Map("x" -> 5.0)),
      Value(Map("x" -> 3.0))
    )

    val result = expr.eval()
    result shouldEqual Value(Map("condition" -> 1.0))
  }

  it should "correctly handle undefined operands during partial evaluation" in {
    val expr = GREATEREQUAL(
      Value(Map("x" -> 5.0)),
      Var("y")
    )

    val result = expr.eval()
    result shouldEqual GREATEREQUAL(Value(Map("x" -> 5.0)), Var("y"))
  }

  "Class instantiation and method invocation" should "work correctly" in {
    // Define a class 'Point' with fields 'x' and 'y' and method 'move'
    val pointClass = ClassDef(
      name = "Pointer",
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
    pointClass.eval()
    Public("Pointer", List("x", "y"), List("move")).eval()

    // Instantiate 'Point' class
    Instantiate("p", "Pointer", Map(
      "x_init" -> Value(Map("" -> 0.0)),
      "y_init" -> Value(Map("" -> 0.0))
    )).eval()

    // Invoke 'move' method
    MethodInvocation(Var("p"), "move", Map(
      "dx" -> Value(Map("" -> 1.0)),
      "dy" -> Value(Map("" -> 2.0))
    )).eval()

    // Check the updated fields
    val fields = getInstanceFields("p", getGlobalScope)
    fields("x") shouldEqual Value(Map("" -> 1.0))
    fields("y") shouldEqual Value(Map("" -> 2.0))
  }

  "Inheritance" should "allow derived classes to inherit from base classes" in {
    // Define base class 'Point'
    val baseClass = ClassDef(
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
    baseClass.eval()
    Public("Point", List("x", "y"), List("move")).eval()

    // Define derived class 'ColoredPoint'
    val derivedClass = ClassDef(
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
    derivedClass.eval()
    Public("ColoredPoint", List("color"), List("setColor")).eval()

    // Instantiate 'ColoredPoint' class
    Instantiate("cp", "ColoredPoint", Map(
      "x_init" -> Value(Map("" -> 0.0)),
      "y_init" -> Value(Map("" -> 0.0)),
      "color_init" -> Value(Map("" -> 1.0))
    )).eval()

    // Invoke 'move' and 'setColor' methods
    MethodInvocation(Var("cp"), "move", Map(
      "dx" -> Value(Map("" -> 1.0)),
      "dy" -> Value(Map("" -> 2.0))
    )).eval()

    MethodInvocation(Var("cp"), "setColor", Map(
      "newColor" -> Value(Map("" -> 2.0))
    )).eval()

    // Check the updated fields
    val fields = getInstanceFields("cp", getGlobalScope)
    fields("x") shouldEqual Value(Map("" -> 1.0))
    fields("y") shouldEqual Value(Map("" -> 2.0))
    fields("color") shouldEqual Value(Map("" -> 2.0))
  }

  "Abstract classes" should "require subclasses to implement abstract methods" in {
    // Define an abstract class 'Shape' with an abstract method 'area'
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
    shapeClass.eval()
    Public("Shape", List("x", "y"), List()).eval()

    // Define a concrete class 'Circle' that extends 'Shape' and implements 'area'
    val circleClass = ClassDef(
      name = "Circle",
      fields = List(Field("radius"), Field("area")), // Added 'area' as a field
      methods = List(
        Method(
          m_name = "area",
          args = List(),
          exp = List(
            Assign(
              "area",
              Multiplication(
                Value(Map("" -> 3.14159)),
                Multiplication(Var("radius"), Var("radius"))
              )
            )
          )
        )
      ),
      constructor = Constructor(
        exp = List(
          Assign("x", Var("x_init")),
          Assign("y", Var("y_init")),
          Assign("radius", Var("radius_init")),
          Assign("area", Value(Map("" -> 0.0))) // Initialize 'area' in the constructor
        )
      ),
      parent = Some(shapeClass) // Use the actual class definition
    )
    circleClass.eval()
    Public("Circle", List("radius", "area"), List("area")).eval()

    // Instantiate 'Circle' class
    Instantiate("c", "Circle", Map(
      "x_init" -> Value(Map("" -> 0.0)),
      "y_init" -> Value(Map("" -> 0.0)),
      "radius_init" -> Value(Map("" -> 5.0))
    )).eval()

    // Invoke 'area' method
    MethodInvocation(Var("c"), "area", Map()).eval()

    // Check the computed 'area'
    val fields = getInstanceFields("c", getGlobalScope)
    fields("area") shouldEqual Value(Map("" -> 78.53975)) // π * r^2 = 3.14159 * 25
  }

  "Union operation" should "support partial evaluation and associativity" in {
    val setA = Map("a" -> 0.2, "b" -> 0.5)
    val setB = Map("b" -> 0.6, "c" -> 0.8)
    val expr = Union(
      Value(setA),
      Union(
        Value(setB),
        Var("setC")
      )
    )

    val result = expr.eval()
    val expectedSet = setA ++ setB.map { case (k, v) =>
      k -> math.max(v, setA.getOrElse(k, 0.0))
    }
    result shouldEqual Union(Value(expectedSet), Var("setC"))

    // Assign 'setC' and fully evaluate
    val setC = Map("c" -> 0.9, "d" -> 0.7)
    getGlobalScope.setVariable("setC", Value(setC))

    val fullResult = expr.eval()
    val combinedSet = expectedSet ++ setC.map { case (k, v) =>
      k -> math.max(v, expectedSet.getOrElse(k, 0.0))
    }
    fullResult shouldEqual Value(combinedSet)
  }

  "Intersection operation" should "support partial evaluation and associativity" in {
    val setA = Map("a" -> 0.8, "b" -> 0.6)
    val setB = Map("b" -> 0.7, "c" -> 0.5)
    val expr = Intersection(
      Value(setA),
      Intersection(
        Value(setB),
        Var("setC")
      )
    )

    val result = expr.eval()
    val expectedSet = setA.filter { case (k, _) => setB.contains(k) }.map { case (k, v) =>
      k -> math.min(v, setB(k))
    }
    result shouldEqual Intersection(Value(expectedSet), Var("setC"))

    // Assign 'setC' and fully evaluate
    val setC = Map("b" -> 0.9, "d" -> 0.7)
    getGlobalScope.setVariable("setC", Value(setC))

    val fullResult = expr.eval()
    val finalSet = expectedSet.filter { case (k, _) => setC.contains(k) }.map { case (k, v) =>
      k -> math.min(v, setC(k))
    }
    fullResult shouldEqual Value(finalSet)
  }

  // Helper methods
  def getGlobalScope: FuzzyScope = FuzzyMath.globalScope

  def getInstanceFields(instanceVarName: String, currentScope: FuzzyScope): Map[String, FuzzyExp] = {
    currentScope.searchBinding(instanceVarName) match {
      case Some(InstanceRef(instanceName)) =>
        FuzzyMath.instanceRegistry.get(instanceName) match {
          case Some(instanceData) =>
            instanceData("fields").asInstanceOf[mutable.Map[String, FuzzyExp]].toMap
          case None =>
            throw new Exception(s"Instance '$instanceName' not found")
        }
      case Some(value) =>
        throw new Exception(s"Variable '$instanceVarName' is bound to an unexpected value: $value")
      case None =>
        throw new Exception(s"Variable '$instanceVarName' not found")
    }
  }
}
