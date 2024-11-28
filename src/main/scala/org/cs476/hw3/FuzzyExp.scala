package org.cs476.hw3

enum FuzzyExp:
  // **Basic Expressions**
  case Value(i: Map[String, Double])
  case Var(s: String)
  case Union(fset1: FuzzyExp, fset2: FuzzyExp)
  case Intersection(fset1: FuzzyExp, fset2: FuzzyExp)
  case XOR(fset1: FuzzyExp, fset2: FuzzyExp)
  case Complement(fset: FuzzyExp)
  case Addition(fset1: FuzzyExp, fset2: FuzzyExp)
  case Multiplication(fset1: FuzzyExp, fset2: FuzzyExp)

  // **Scoping and Blocks**
  case Scope(name: String, body: FuzzyExp)
  case Assign(name: String, expr: FuzzyExp)
  case Block(expressions: List[FuzzyExp])

  // **Macros**
  case MacroDef(name: String, params: List[String], body: FuzzyExp)
  case MacroInvoke(name: String, args: List[FuzzyExp])

  // **Classes and Objects**
  case Field(f_name: String)
  case Method(m_name: String, args: List[Assign], exp: List[FuzzyExp])
  case Constructor(exp: List[FuzzyExp])
  case ClassDef(
                 name: String,
                 fields: List[Field],
                 methods: List[Method],
                 constructor: Constructor,
                 parent: Option[FuzzyExp]
               )
  case InstanceRef(name: String) extends FuzzyExp
  case Instantiate(varName: String, className: String, args: Map[String, FuzzyExp])
  case MethodInvocation(instanceVar: FuzzyExp, methodName: String, arguments: Map[String, FuzzyExp])

  // **Access Control**
  case Public(name: String, fieldNameList: List[String], methodNameList: List[String])
  case Private(name: String, fieldNameList: List[String], methodNameList: List[String])
  case Protected(name: String, fieldNameList: List[String], methodNameList: List[String])

  // **Abstract Classes and Methods**
  case AbstractMethod(name: String)
  case AbstractClassDef(
                         name: String,
                         fields: List[Field],
                         concreteMethods: List[Method],
                         abstractMethods: List[AbstractMethod],
                         constructor: Constructor
                       )

  // **Conditional Constructs**
  case IFTRUE(cond: FuzzyExp, thenBranch: FuzzyExp, elseBranch: FuzzyExp)

  // **Comparison Operators**
  case GREATEREQUAL(lhs: FuzzyExp, rhs: FuzzyExp)
  case LESSERTHAN(lhs: FuzzyExp, rhs: FuzzyExp)
  case EQUAL(lhs: FuzzyExp, rhs: FuzzyExp)
  case GREATERTHAN(lhs: FuzzyExp, rhs: FuzzyExp)
  case LESSEROREQUAL(lhs: FuzzyExp, rhs: FuzzyExp)

  // **Logical Operators**
  case AND(lhs: FuzzyExp, rhs: FuzzyExp)
  case OR(lhs: FuzzyExp, rhs: FuzzyExp)
  case NOT(exp: FuzzyExp)

  // **Additional Arithmetic Operations (Optional)**
  case Subtraction(fset1: FuzzyExp, fset2: FuzzyExp)
  case Division(fset1: FuzzyExp, fset2: FuzzyExp)

  // **Loops (Optional)**
  case WhileLoop(condition: FuzzyExp, body: FuzzyExp)
  case ForLoop(variable: String, iterable: FuzzyExp, body: FuzzyExp)

  // **Function Definitions and Calls (Optional)**
  case FunctionDef(name: String, params: List[String], body: FuzzyExp)
  case FunctionCall(name: String, args: List[FuzzyExp])
