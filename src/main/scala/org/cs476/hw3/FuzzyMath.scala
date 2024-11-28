package org.cs476.hw3

import org.cs476.hw3.utils.CreateLogger
import org.slf4j.Logger

import scala.collection.mutable

object FuzzyMath:
  val logger: Logger = CreateLogger(this.getClass)
  private type FuzzySet = Map[String, Double]
  import FuzzyExp._

  val globalScope = new FuzzyScope("global", None)
  private val scopeRegistry: mutable.Map[String, FuzzyScope] = mutable.Map.empty

  // Class Registry to store class definitions
  private val classRegistry: mutable.Map[String, mutable.Map[String, Any]] = mutable.Map.empty

  // Access control registry to keep track of Private, Protected, and Public access levels
  private val accessControlRegistry: mutable.Map[String, mutable.Map[String, mutable.Map[String, mutable.Set[String]]]] = mutable.Map.empty

  // Instance registry to store instantiated objects
  val instanceRegistry: mutable.Map[String, mutable.Map[String, Any]] = mutable.Map.empty

  // Virtual Dispatch Table for maintaining methods for inherited classes.
  private val virtualDispatchTable: mutable.Map[String, mutable.Map[String, Method]] = mutable.Map()

  // Registry to store macros
  private val macroRegistry: mutable.Map[String, (List[String], FuzzyExp)] = mutable.Map.empty

  // Provide a method to get the global scope
  def getGlobalScope: FuzzyScope = globalScope

  // Provide a method to get instance fields
  def getInstanceFields(instanceVarName: String, currentScope: FuzzyScope): Map[String, Any] =
    currentScope.searchBinding(instanceVarName) match
      case Some(InstanceRef(instanceName)) =>
        instanceRegistry.get(instanceName) match
          case Some(instanceData) =>
            instanceData("fields").asInstanceOf[mutable.Map[String, Any]].toMap
          case None =>
            throw new Exception(s"Instance '$instanceName' not found")
      case Some(value) =>
        throw new Exception(s"Variable '$instanceVarName' is bound to an unexpected value: $value")
      case None =>
        throw new Exception(s"Variable '$instanceVarName' not found")

  def resetGlobalScope(): Unit =
    globalScope.clearBindings()
    scopeRegistry.clear()

  def round(value: Double, places: Int = 6): Double =
    val scale = math.pow(10, places)
    (value * scale).round / scale

  extension (exp: FuzzyExp)
    def eval(): FuzzyExp =
      exp.evalInScope(globalScope)

    private def evalInScope(currentScope: FuzzyScope): FuzzyExp = exp match
      case Value(i) => Value(i)

        case Var(name) =>
        currentScope.searchBinding(name) match
          case Some(value: FuzzyExp) =>
            value.evalInScope(currentScope)
          case Some(value: Map[String, Double]) =>
            Value(value)
          case Some(value: String) =>
            InstanceRef(value)
          case Some(value) =>
            throw new Exception(s"Variable '$name' is bound to an unexpected value: $value")
          case None =>
            Var(name)
          case other =>
            throw new Exception(s"Variable '$name' is bound to an unexpected value: $other")

      case Assign(name, expr) =>
        val value = expr.evalInScope(currentScope)
        currentScope.setVariable(name, value)
        currentScope.logger.info(s"Assigned Variable('$name') in scope '${currentScope.name}' with value: $value")
        value

      case Scope(scopeName, body) =>
        val newScope = scopeRegistry.getOrElseUpdate(scopeName, currentScope.getOrCreateChildScope(scopeName))
        currentScope.logger.info(s"Entering Scope('$scopeName')")
        val result = body.evalInScope(newScope)
        currentScope.logger.info(s"Exiting Scope('$scopeName')")
        result

      case Block(expressions) =>
        val results = expressions.map(_.evalInScope(currentScope))
        results.lastOption.getOrElse(Value(Map.empty[String, Double]))

      case Union(fset1, fset2) =>
        val left = fset1.evalInScope(currentScope)
        val right = fset2.evalInScope(currentScope)

        (left, right) match
          case (Value(set1), Value(set2)) =>
            val result = set1 ++ set2.map { case (k, v) =>
              k -> round(math.max(v, set1.getOrElse(k, 0.0)))
            }
            Value(result)

          case (Value(set1), Union(Value(set2), exp)) =>
            // Apply associativity: (A ∪ (B ∪ C)) => ((A ∪ B) ∪ C)
            val newSet = set1 ++ set2.map { case (k, v) =>
              k -> round(math.max(v, set1.getOrElse(k, 0.0)))
            }
            Union(Value(newSet), exp)

          case (Union(Value(set1), exp1), Value(set2)) =>
            // Apply associativity: ((A ∪ B) ∪ C) => (A ∪ (B ∪ C))
            val newSet = set1 ++ set2.map { case (k, v) =>
              k -> round(math.max(v, set1.getOrElse(k, 0.0)))
            }
            Union(Value(newSet), exp1)

          case _ =>
            Union(left, right)

      case Intersection(fset1, fset2) =>
        val left = fset1.evalInScope(currentScope)
        val right = fset2.evalInScope(currentScope)

        (left, right) match
          case (Value(set1), Value(set2)) =>
            val result = set1.filter { case (k, _) => set2.contains(k) }.map { case (k, v) =>
              k -> round(math.min(v, set2(k)))
            }
            Value(result)

          case (Value(set1), Intersection(Value(set2), exp)) =>
            // Apply associativity: (A ∩ (B ∩ C)) => ((A ∩ B) ∩ C)
            val newSet = set1.filter { case (k, _) => set2.contains(k) }.map { case (k, v) =>
              k -> round(math.min(v, set2(k)))
            }
            Intersection(Value(newSet), exp)

          case (Intersection(Value(set1), exp1), Value(set2)) =>
            // Apply associativity: ((A ∩ B) ∩ C) => (A ∩ (B ∩ C))
            val newSet = set1.filter { case (k, _) => set2.contains(k) }.map { case (k, v) =>
              k -> round(math.min(v, set2(k)))
            }
            Intersection(Value(newSet), exp1)

          case _ =>
            Intersection(left, right)


      case Addition(fset1, fset2) =>
        val left = fset1.evalInScope(currentScope)
        val right = fset2.evalInScope(currentScope)

        (left, right) match
          case (Value(set1), Value(set2)) =>
            val result = set1 ++ set2.map { case (k, v) =>
              k -> round(v + set1.getOrElse(k, 0.0))
            }
            Value(result)

          case (Value(set1), Addition(Value(set2), exp)) =>
            // Apply associativity: (A + (B + C)) => ((A + B) + C)
            val newSet = set1 ++ set2.map { case (k, v) =>
              k -> round(v + set1.getOrElse(k, 0.0))
            }
            Addition(Value(newSet), exp)

          case (Addition(Value(set1), exp1), Value(set2)) =>
            // Apply associativity: ((A + B) + C) => (A + (B + C))
            val newSet = set1 ++ set2.map { case (k, v) =>
              k -> round(v + set1.getOrElse(k, 0.0))
            }
            Addition(Value(newSet), exp1)

          case _ =>
            Addition(left, right)


      case Multiplication(fset1, fset2) =>
        val left = fset1.evalInScope(currentScope)
        val right = fset2.evalInScope(currentScope)

        (left, right) match
          case (Value(set1), Value(set2)) =>
            // Multiply the sets directly
            val result = set1.map { case (k, v) =>
              k -> round(v * set2.getOrElse(k, 1.0))
            }
            Value(result)

          case (Value(set1), Multiplication(Value(set2), exp)) =>
            // Apply associativity: (A * (B * C)) => ((A * B) * C)
            val newSet = set1.map { case (k, v) =>
              k -> round(v * set2.getOrElse(k, 1.0))
            }
            Multiplication(Value(newSet), exp)

          case (Multiplication(Value(set1), exp1), Value(set2)) =>
            // Apply associativity: ((A * B) * C) => (A * (B * C))
            val newSet = set1.map { case (k, v) =>
              k -> round(v * set2.getOrElse(k, 1.0))
            }
            Multiplication(Value(newSet), exp1)

          case _ =>
            Multiplication(left, right)


      case Complement(fset) =>
        val set = fset.evalInScope(currentScope)
        set match
          case Value(setMap) =>
            val result = setMap.map { case (k, v) => k -> round(1.0 - v) }
            Value(result)
          case _ =>
            Complement(set)

      case XOR(fset1, fset2) =>
        val left = fset1.evalInScope(currentScope)
        val right = fset2.evalInScope(currentScope)

        (left, right) match
          case (Value(set1), Value(set2)) =>
            val allKeys = set1.keySet ++ set2.keySet
            val result = allKeys.map { key =>
              val val1 = set1.getOrElse(key, 0.0)
              val val2 = set2.getOrElse(key, 0.0)
              key -> round(math.abs(val1 - val2))
            }.toMap
            Value(result)

          case (Value(set1), XOR(Value(set2), exp)) =>
            // Apply associativity: (A XOR (B XOR C)) => ((A XOR B) XOR C)
            val allKeys = set1.keySet ++ set2.keySet
            val newSet = allKeys.map { key =>
              val val1 = set1.getOrElse(key, 0.0)
              val val2 = set2.getOrElse(key, 0.0)
              key -> round(math.abs(val1 - val2))
            }.toMap
            XOR(Value(newSet), exp)

          case (XOR(Value(set1), exp1), Value(set2)) =>
            // Apply associativity: ((A XOR B) XOR C) => (A XOR (B XOR C))
            val allKeys = set1.keySet ++ set2.keySet
            val newSet = allKeys.map { key =>
              val val1 = set1.getOrElse(key, 0.0)
              val val2 = set2.getOrElse(key, 0.0)
              key -> round(math.abs(val1 - val2))
            }.toMap
            XOR(Value(newSet), exp1)

          case _ =>
            XOR(left, right)


      case MacroDef(name, params, body) =>
        macroRegistry(name) = (params, body)
        logger.info(s"Defined macro '$name'")
        Value(Map.empty[String, Double])

      case MacroInvoke(name, args) =>
        val (params, body) = macroRegistry.getOrElse(name, throw new Exception(s"Macro '$name' not found"))
        if params.length != args.length then
          throw new Exception(s"Macro '$name' expects ${params.length} arguments, but got ${args.length}")

        val macroScope = new FuzzyScope(s"${name}_macro_scope", Some(currentScope))
        params.zip(args).foreach { case (param, arg) =>
          val argValue = arg.evalInScope(currentScope)
          macroScope.createBinding(param, argValue)
        }
        body.evalInScope(macroScope)

      case AbstractClassDef(name, fields, concreteMethods, abstractMethods, constructor) =>
        if classRegistry.contains(name) then
          throw new Exception(s"Class '$name' already defined")

        val abstractMethodNames = abstractMethods.map(_.name)
        val fieldMap = fields.map(f => f.f_name -> null).to(mutable.Map)
        val methodMap = concreteMethods.map(m => m.m_name -> m).to(mutable.Map)

        classRegistry(name) = mutable.Map(
          "fields" -> fieldMap,
          "methods" -> methodMap,
          "abstract" -> true,
          "abstractMethods" -> abstractMethodNames,
          "constructor" -> constructor
        )

        // Initialize access control for abstract class
        accessControlRegistry(name) = mutable.Map(
          "private" -> mutable.Map("fields" -> mutable.Set[String](), "methods" -> mutable.Set[String]()),
          "public" -> mutable.Map("fields" -> mutable.Set[String](), "methods" -> mutable.Set[String]()),
          "protected" -> mutable.Map("fields" -> mutable.Set[String](), "methods" -> mutable.Set[String]())
        )

        logger.info(s"Defined abstract class '$name' with abstract methods: ${abstractMethodNames.mkString(", ")}")
        Value(Map.empty[String, Double])

      case ClassDef(name, fields, methods, constructor, parent) =>
        if classRegistry.contains(name) then
          throw new Exception(s"Class '$name' already defined")
        else
          val tempClassMap = mutable.Map[String, Any](
            "fields" -> fields.map(f => f.f_name -> null).to(mutable.Map),
            "constructor" -> constructor,
            "methods" -> methods.map(m => m.m_name -> m).to(mutable.Map),
            "inheritance" -> false,
            "abstract" -> false
          )

          // Handle inheritance and check if parent is abstract
          parent.foreach {
            case abstractClass: AbstractClassDef =>
              val abstractMethods = abstractClass.abstractMethods.map(_.name)
              abstractMethods.foreach { methodName =>
                if !methods.exists(_.m_name == methodName) then
                  throw new Exception(s"Class '$name' must implement abstract method '$methodName' from abstract class '${abstractClass.name}'")
              }

              // Copy fields and methods from abstract class
              tempClassMap("fields").asInstanceOf[mutable.Map[String, Any]] ++= abstractClass.fields.map(f => f.f_name -> null).toMap
              tempClassMap("methods").asInstanceOf[mutable.Map[String, Method]] ++= abstractClass.concreteMethods.map(m => m.m_name -> m).toMap

            case concreteClass: ClassDef =>
              // Handle concrete class parent (existing logic)
              tempClassMap("fields").asInstanceOf[mutable.Map[String, Any]] ++= concreteClass.fields.map(f => f.f_name -> null).toMap
              tempClassMap("methods").asInstanceOf[mutable.Map[String, Method]] ++= concreteClass.methods.map(m => m.m_name -> m).toMap

            case _ =>
              throw new Exception("Invalid parent class type")
          }

          classRegistry(name) = tempClassMap

          // Initialize access control for the class
          accessControlRegistry(name) = mutable.Map(
            "private" -> mutable.Map("fields" -> mutable.Set[String](), "methods" -> mutable.Set[String]()),
            "public" -> mutable.Map("fields" -> mutable.Set[String](), "methods" -> mutable.Set[String]()),
            "protected" -> mutable.Map("fields" -> mutable.Set[String](), "methods" -> mutable.Set[String]())
          )

          logger.info(s"Defined class '$name'")
          Value(Map.empty[String, Double])

      case InstanceRef(name) =>
      // Since InstanceRef is a reference to an instance, and there's no further evaluation needed,
      // we can return the InstanceRef itself.
        exp

      case Public(className, fieldNameList, methodNameList) =>
        if !accessControlRegistry.contains(className) then
          throw new Exception(s"Access control data for class '$className' not found")
        else
          val accessData = accessControlRegistry(className)
          val publicFields = accessData("public")("fields")
          val publicMethods = accessData("public")("methods")
          publicFields ++= fieldNameList
          publicMethods ++= methodNameList
          logger.info(s"Updated public access for class '$className'")
          Value(Map.empty[String, Double])

      case Private(className, fieldNameList, methodNameList) =>
        if !accessControlRegistry.contains(className) then
          throw new Exception(s"Access control data for class '$className' not found")
        else
          val accessData = accessControlRegistry(className)
          val privateFields = accessData("private")("fields")
          val privateMethods = accessData("private")("methods")
          privateFields ++= fieldNameList
          privateMethods ++= methodNameList
          logger.info(s"Updated private access for class '$className'")
          Value(Map.empty[String, Double])

      case Protected(className, fieldNameList, methodNameList) =>
        if !accessControlRegistry.contains(className) then
          throw new Exception(s"Access control data for class '$className' not found")
        else
          val accessData = accessControlRegistry(className)
          val protectedFields = accessData("protected")("fields")
          val protectedMethods = accessData("protected")("methods")
          protectedFields ++= fieldNameList
          protectedMethods ++= methodNameList
          logger.info(s"Updated protected access for class '$className'")
          Value(Map.empty[String, Double])

      case Instantiate(varName, className, args) =>
        if !classRegistry.contains(className) then
          throw new Exception(s"Class '$className' not found")
        if classRegistry(className).getOrElse("abstract", false).asInstanceOf[Boolean] then
          throw new Exception(s"Cannot instantiate abstract class '$className'")

        val instanceName = s"${className}_instance_${instanceRegistry.size}"
        val tempNewObjectMap = mutable.Map[String, Any]("className" -> className)
        val classObject = classRegistry(className)
        val classFields = classObject("fields").asInstanceOf[mutable.Map[String, Any]].clone()
        val classMethods = classObject("methods").asInstanceOf[mutable.Map[String, Method]]

        tempNewObjectMap += ("fields" -> classFields)
        tempNewObjectMap += ("methods" -> classMethods)
        instanceRegistry(instanceName) = tempNewObjectMap

        val constructor = classObject("constructor").asInstanceOf[Constructor]
        val constructorScope = new FuzzyScope(s"${className}_constructor", Some(currentScope))
        classFields.foreach { case (k, v) => constructorScope.createBinding(k, v.asInstanceOf[FuzzyExp]) }
        args.foreach { case (k, vExp) => constructorScope.createBinding(k, vExp.evalInScope(currentScope)) }

        constructor.exp.foreach { exp => exp.evalInScope(constructorScope) }
        classFields.keys.foreach { k => classFields(k) = constructorScope.searchBinding(k).getOrElse(classFields(k)) }
        currentScope.createBinding(varName, InstanceRef(instanceName))

        logger.info(s"Created instance '$instanceName' of class '$className'")
        Value(Map.empty[String, Double])

      case MethodInvocation(instanceVar, methodName, arguments) =>
        val instanceNameExp = instanceVar.evalInScope(currentScope)
        val instanceName = instanceNameExp match
          case InstanceRef(name) => name
          case _ =>
            throw new Exception(s"Invalid instance variable '$instanceVar'")
//
//          case Var(name) =>
//            currentScope.searchBinding(name) match
//              case Some(value: String) => value
//              case Some(value: FuzzyExp) =>
//                value.evalInScope(currentScope) match
//                  case Value(map) if map.contains("instanceName") => map("instanceName").toString
//                  case _ => throw new Exception(s"Cannot resolve instance variable '$name'")
//              case None => throw new Exception(s"Variable '$name' not found")
//              case _ => throw new Exception(s"Variable '$name' does not contain a valid instance")
//          case _ => throw new Exception(s"Invalid instance variable '$instanceVar'")

        if !instanceRegistry.contains(instanceName) then
          throw new Exception(s"Instance '$instanceName' not found")

        val instanceData = instanceRegistry(instanceName)
        val className = instanceData("className").asInstanceOf[String]

        // Check access control for the method
        val accessData = accessControlRegistry.getOrElse(className, throw new Exception(s"Access control data for class '$className' not found"))
        val isPublic = accessData("public")("methods").contains(methodName)
        val isPrivate = accessData("private")("methods").contains(methodName)
        val isProtected = accessData("protected")("methods").contains(methodName)

        if isPrivate then throw new Exception(s"Method '$methodName' is private and cannot be accessed")

        val methods = instanceData("methods").asInstanceOf[mutable.Map[String, Method]]
        val methodToInvoke = virtualDispatchTable.getOrElse(className, methods).getOrElse(
          methodName,
          throw new Exception(s"Method '$methodName' not found in class '$className'")
        )

        val methodScope = new FuzzyScope(s"${className}_$methodName", Some(currentScope))

        // Partially evaluate arguments
        val partiallyEvaluatedArgs = arguments.map { case (argName, argExp) =>
          argName -> argExp.evalInScope(currentScope)
        }

        // Set method arguments in scope
        methodToInvoke.args.foreach {
          case Assign(argName, _) =>
            val argValueExp = partiallyEvaluatedArgs.getOrElse(
              argName,
              throw new Exception(s"Argument '$argName' not provided")
            )
            methodScope.createBinding(argName, argValueExp)
        }

        // Set instance fields in scope
        val instanceFields = instanceData("fields").asInstanceOf[mutable.Map[String, Any]]
        instanceFields.foreach { case (k, v) =>
          methodScope.createBinding(k, v.asInstanceOf[FuzzyExp])
        }

        // Partially evaluate method body
        val partiallyEvaluatedBody = methodToInvoke.exp.map(_.evalInScope(methodScope))

        // Update instance fields after method execution
        instanceFields.keys.foreach { k =>
          instanceFields(k) = methodScope.searchBinding(k).getOrElse(instanceFields(k))
        }

        logger.info(s"Invoked method '$methodName' on instance '$instanceName'")
        partiallyEvaluatedBody.lastOption.getOrElse(Value(Map.empty[String, Double]))

      case IFTRUE(cond, thenBranch, elseBranch)  =>
        val conditionResult = cond.evalInScope(currentScope)
        conditionResult match
          case Value(condValue) =>
            if evaluateCondition(condValue) then
              thenBranch.evalInScope(currentScope)
            else
              elseBranch.evalInScope(currentScope)
          case _ =>
            // Condition cannot be fully evaluated; partially evaluate both branches
            val thenPart = thenBranch.evalInScope(currentScope)
            val elsePart = elseBranch.evalInScope(currentScope)
            IFTRUE(conditionResult, thenPart, elsePart)

      case GREATEREQUAL(lhs, rhs) =>
        val left = lhs.evalInScope(currentScope)
        val right = rhs.evalInScope(currentScope)
        (left, right) match
          case (Value(set1), Value(set2)) =>
            val result = compareSets(set1, set2, _ >= _)
            Value(Map("condition" -> (if result then 1.0 else 0.0)))
          case _ =>
            GREATEREQUAL(left, right)

  // Implement other comparison and logical operators as needed

  // Helper functions
  private def evaluateCondition(condValue: Map[String, Double]): Boolean =
    condValue.get("condition").exists(_ >= 1.0)

  private def compareSets(
                           set1: Map[String, Double],
                           set2: Map[String, Double],
                           comparator: (Double, Double) => Boolean
                         ): Boolean =
    // Simplified comparison: compare the sums of the sets
    val sum1 = set1.values.sum
    val sum2 = set2.values.sum
    comparator(sum1, sum2)
