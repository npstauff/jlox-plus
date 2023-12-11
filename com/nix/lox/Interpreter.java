package com.nix.lox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nix.lox.Expr.Array;
import com.nix.lox.Expr.Assign;
import com.nix.lox.Expr.Binary;
import com.nix.lox.Expr.Call;
import com.nix.lox.Expr.Cast;
import com.nix.lox.Expr.Get;
import com.nix.lox.Expr.GetIndex;
import com.nix.lox.Expr.GetStatic;
import com.nix.lox.Expr.Grouping;
import com.nix.lox.Expr.Length;
import com.nix.lox.Expr.Literal;
import com.nix.lox.Expr.Logical;
import com.nix.lox.Expr.New;
import com.nix.lox.Expr.Set;
import com.nix.lox.Expr.SetAssign;
import com.nix.lox.Expr.SetIndex;
import com.nix.lox.Expr.Super;
import com.nix.lox.Expr.Ternary;
import com.nix.lox.Expr.This;
import com.nix.lox.Expr.Type;
import com.nix.lox.Expr.Unary;
import com.nix.lox.Expr.Value;
import com.nix.lox.Expr.Variable;
import com.nix.lox.LoxType.TypeEnum;
import com.nix.lox.Stmt.Block;
import com.nix.lox.Stmt.Break;
import com.nix.lox.Stmt.Case;
import com.nix.lox.Stmt.Class;
import com.nix.lox.Stmt.Expect;
import com.nix.lox.Stmt.Expression;
import com.nix.lox.Stmt.Function;
import com.nix.lox.Stmt.GetFile;
import com.nix.lox.Stmt.If;
import com.nix.lox.Stmt.Property;
import com.nix.lox.Stmt.Return;
import com.nix.lox.Stmt.Switch;
import com.nix.lox.Stmt.Try;
import com.nix.lox.Stmt.Var;
import com.nix.lox.Stmt.While;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

  public static Interpreter current;

  final Environment globals = new Environment(this);
  Environment environment = globals;
  private final Map<Token, Integer> locals = new HashMap<>();

  Interpreter(){
    defineNativeFunctions();
    defineNativeClasses();
    current = this;
  }

  

  private void defineNativeFunctions(){
    

    globals.define("throw", new LoxCallable() {

      @Override
      public int arity() {
        return 1;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments, List<LoxClass> templates) {
        throw new RuntimeError(new Token(TokenType.EOF, "err", null, -1), "["+"ERROR THROWN"+"] "+arguments.get(0).toString());
      }
      
    }, new Modifiers(TokenType.CONST));

  }

  public String methodNameFromOperator(TokenType operator) {
    switch(operator){
      case BANG_EQUAL: return "notEqual";
      case EQUAL_EQUAL: return "equal";
      case GREATER: return "greater";
      case GREATER_EQUAL: return "greaterEqual";
      case LESS: return "less";
      case LESS_EQUAL: return "lessEqual";
      case MINUS: return "subtract";
      case PLUS: return "add";
      case SLASH: return "divide";
      case STAR: return "multiply";
      case NULL_EQUAL: return "nullNotEqual";
      case NULL_EQUAL_EQUAL: return "nullEqual";
      default: return null;
    }
  }

  Object callOperator(Object left, Object right, boolean leftInst, boolean rightInst, String methodName, Token operator) {
        Object methodObj = null;
        if(leftInst){
          methodObj = ((LoxInstance)left).get(methodName, false, "Operator '" + methodName + "' not found on object '" + ((LoxInstance)left).klass.name + "'");
        }
        else if(rightInst){
          methodObj = ((LoxInstance)right).get(methodName, false, "Operator '" + methodName + "' not found on object '"  + ((LoxInstance)right).klass.name + "'");
        }
        if(methodObj != null) {
          if(methodObj instanceof LoxFunction){
            LoxFunction method = (LoxFunction)methodObj;
            if(method.arity() != 2) {
              throw new RuntimeError(operator, "Operator '" + methodName + "' must take two arguments");
            }
            List<Object> args = new ArrayList<>();
            args.add(leftInst ? (LoxInstance)left : left);
            args.add(rightInst ? (LoxInstance)right : right);
            return method.callFunction(this, args, true, new ArrayList<>());
          }
          else{
            String name = leftInst ? ((LoxInstance)left).klass.name : ((LoxInstance)right).klass.name;
            throw new RuntimeError(operator, "Object '"+name+"' must implement operator '" + methodName + "' ");
          }
        }
        return null;
  }

  @Override
  public Object visitBinaryExpr(Binary expr) {
    Object left = evaluate(expr.left);
    Object right = evaluate(expr.right);

    if(left instanceof Integer){
      left = (Integer)left+0.0;
    }
    if(right instanceof Integer){
      right = (Integer)right+0.0;
    }

    String methodName = methodNameFromOperator(expr.operator.type);
    if(left instanceof LoxInstance && right instanceof LoxInstance){
      LoxInstance leftInstance = (LoxInstance)left;
      LoxInstance rightInstance = (LoxInstance)right;

      if(leftInstance.klass.name.equals(rightInstance.klass.name)){
        return callOperator(left, right, true, true, methodName, expr.operator);
      }
    }
    else if (left instanceof LoxInstance && !(right instanceof LoxInstance)){
      return callOperator(left, right, true, false, methodName, expr.operator);
    }
    else if (right instanceof LoxInstance && !(left instanceof LoxInstance)){
      return callOperator(left, right, false, true, methodName, expr.operator);
    } 

    switch(expr.operator.type){
        case NULL_EQUAL:{
          if(left == null || right == null){
            return null;
          }
          else{
            return isEqual(left, right);
          }
        }

        case NULL_EQUAL_EQUAL:{
          if(left == null){
            return right;
          }
          else{
            return left;
          }
        }

        case BANG_EQUAL: return !isEqual(left, right);
        case EQUAL_EQUAL: return isEqual(left, right);

        case GREATER:
          checkNumberOperands(expr.operator, left, right);
          return (double)left > (double)right;
        case GREATER_EQUAL:
          checkNumberOperands(expr.operator, left, right);
          return (double)left >= (double)right;
        case LESS:
          checkNumberOperands(expr.operator, left, right);
          return (double)left < (double)right;
        case LESS_EQUAL:
          checkNumberOperands(expr.operator, left, right);
          return (double)left <= (double)right;

        case MINUS:
          checkNumberOperands(expr.operator, left, right);
          return (double)left - (double)right;
        case PLUS:
          if (left instanceof Double && right instanceof Double) {
            return (double)left + (double)right;
          } 
          else {
            return left.toString() + right.toString();
          }
          case SLASH:
          checkNumberOperands(expr.operator, left, right);
          return (double)left / (double)right;
        case STAR:
          checkNumberOperands(expr.operator, left, right);
          return (double)left * (double)right;
      default:
        break;
        }
    return null;
  }      
          //throw new RuntimeError(expr.operator, "Operands must be two numbers or strings");
        

  @Override
  public Object visitGroupingExpr(Grouping expr) {
    return evaluate(expr.expression);
  }

  @Override
  public Object visitLiteralExpr(Literal expr) {
    return expr.value;
  }

  @Override
  public Object visitUnaryExpr(Unary expr) {
    Object right = evaluate(expr.right);

    switch (expr.operator.type){
      case BANG:
        return !isTruthy(right);
      case MINUS:
        checkNumberOperand(expr.operator, right);
        return -(double)right;
      default:
        break;
    }

    return null;
  }

  private void checkNumberOperand(Token operator, Object operand){
    if(operand instanceof Double) return;
    throw new RuntimeError(operator, "Operand must be a number");
  }

  private void checkNumberOperands(Token operator, Object left, Object right){
    if(left instanceof Double && right instanceof Double) return;
    throw new RuntimeError(operator, "Operand must be a number");
  }

  private boolean isTruthy(Object object) {
    if(object == null) return false;
    if(object instanceof Boolean) return (boolean)object;
    return true;
  }

  private boolean isEqual(Object a, Object b) {
    if(a == null && b == null) return true;
    if(a == null) return false;

    return a.equals(b);
  }

  private Object evaluate(Expr expr) {
    return expr.accept(this);
  }

  void interpret(List<Stmt> statements){
    try{
      for(Stmt statement : statements){
        execute(statement);
      }
    } catch(RuntimeError e){
      Lox.runtimeError(e);
    }
  }

  private void execute(Stmt stmt) {
    stmt.accept(this);
  }

  void resolve(Token expr, int depth){
    locals.put(expr, depth);
  }

  @Override
  public Void visitExpressionStmt(Expression stmt) {
    evaluate(stmt.expression);
    return null;
  }

  public static void checkModifiers(Modifiers modifiers, LoxType type, Object value, String s) {
      checkUnsigned(modifiers, type, value, s);
      checkByte(modifiers, type, value, s);
  }

  private static void checkByte(Modifiers modifiers, LoxType type, Object value, String s) {
    if(value instanceof LoxProperty) {
      Object val = ((LoxProperty)value).get(Interpreter.current);
      checkByte(modifiers, new LoxType(val), val, s);
      return;
    }
    if(type.type == TypeEnum.NUMBER && modifiers.contains(TokenType.BYTE)) {
      if(value instanceof LoxArray) {
        LoxArray array = (LoxArray)value;
        for(Object o : array.values()) {
          if((Double)o > 255 || (Double)o < 0) {
            throw new RuntimeError(new Token(TokenType.BYTE, "byte", null, 0), "Can't assign value outside of byte range to byte array '" + s + "'");
          }
        }
      }
      else if((Double)value > 255 || (Double)value < 0) {
        throw new RuntimeError(new Token(TokenType.BYTE, "byte", null, 0), "Can't assign value outside of byte range to byte variable '" + s + "'");
      }
    }
  }

  private static void checkUnsigned(Modifiers modifiers, LoxType type, Object value, String s) {
    if(value instanceof LoxProperty) {
      Object val = ((LoxProperty)value).get(Interpreter.current);
      checkUnsigned(modifiers, new LoxType(val), val, s);
      return;
    }
    if(value instanceof LoxArray) {
      LoxArray array = (LoxArray)value;
      for(Object o : array.values()) {
        checkUnsigned(modifiers, new LoxType(o), o, s);
      }
      return;
    }
    if(type.type == TypeEnum.NUMBER && modifiers.contains(TokenType.UNSIGNED)) {
      if((Double)value < 0) {
        throw new RuntimeError(new Token(TokenType.UNSIGNED, "unsigned", null, 0), "Can't assign negative value to unsigned variable '" + s + "'");
      }
    }
    else if(type.type != TypeEnum.NUMBER && modifiers.contains(TokenType.UNSIGNED)) {
      throw new RuntimeError(new Token(TokenType.UNSIGNED, "unsigned", null, 0), "Can't assign unsigned modifier to non-number variable '" + s + "'");
    }
  }

  public static void checkModifiers(Field f, String name) {
    if(f.type == null) return;
    checkModifiers(f.modifiers, f.type, f.value, name);
  }

  public static void checkModifiers(Stmt.Var s, Object value, String name) {
    checkModifiers(s.modifiers, s.type, value, name);
  }

  @Override
  public Void visitVarStmt(Var stmt) {
    Object value = null;
    if(stmt.initializer != null) {
      value = evaluate(stmt.initializer);
    }
    else{
      value = defaultValue(stmt.type);
    }
    if(environment.enclosing == null && stmt.modifiers.contains(TokenType.STATIC)) {
      throw new RuntimeError(stmt.name, "Cannot declare static variable in global scope");
    }
    checkModifiers(stmt, value, stmt.name.lexeme);
    environment.define(stmt.name.lexeme, value, stmt.modifiers, stmt.type);
    return null;
  }

  @Override
  public Object visitVariableExpr(Variable expr) {
    return lookUpVariable(expr.name, expr);
  }

  private Object lookUpVariable(Token name, Expr expr) {
    Integer distance = locals.get(name);
    if(distance != null){
      return environment.getAt(distance, name.lexeme);
    } else {
      return globals.get(name);
    }
  }

  boolean functionExists(String name){
    return environment.values.get(name) != null;
  }

  boolean globalFunctionExists(String name){
    return environment.values.get(name) != null;
  }

  @Override
  public Object visitAssignExpr(Assign expr) {
    Object right = evaluate(expr.value);
    Modifiers modifiers = Modifiers.empty();
    modifiers = environment.values.get(expr.name.lexeme) != null ? environment.values.get(expr.name.lexeme).modifiers : null;
    LoxType type = environment.values.get(expr.name.lexeme) != null ? environment.values.get(expr.name.lexeme).type : null;
    Integer distance = locals.get(expr.name);

    boolean hasObject = distance != null ? 
      environment.getAt(distance, expr.name.lexeme) != null : environment.get(expr.name) != null;

    if(hasObject){
      boolean leftIsDouble = false;
      boolean rightIsDouble = false;
      Object left = null;
      left = distance != null ? environment.getAt(distance, expr.name.lexeme) : environment.get(expr.name);
      if(left != null) {
        Field f = distance != null ? environment.getFieldAt(distance, expr.name.lexeme) : environment.getField(expr.name);
        if(f.value instanceof LoxProperty) {
          LoxProperty property = (LoxProperty) f.value;
          left = property;
          ArrayList<Object> args = new ArrayList<>();
          args.add(right);
          if(property.getSet() != null) property.value = property.set(this, args);
          else throw new RuntimeError(expr.name, "Cannot assign to property '" + expr.name.lexeme + "' because it is read-only");

          if(distance != null){
            environment.assignAt(distance, expr.name, property, modifiers, type);
          } else {
            globals.assign(expr.name, property);
          }
          return right;
        }
      }

      if(!validAssignment(left, right)){
        Lox.error(new Token(TokenType.EQUAL, left.toString(), 0, 0), "Not a valid assignment");
        return null;
      }
      int leftD = 0;
      int rightD = 0;
      String leftS = "";
      String rightS = "";

      if(notSpecialAssignment(expr.type)){
        if(right instanceof Double){
          rightIsDouble = true;
          rightD = (int)((double)right);
        }
        else{
          rightS = right.toString();
        }
      }
      if(left instanceof Double){
        leftIsDouble = true;
        leftD = (int)((double)left);
      }
      else{
        leftS = left.toString();
      }
      
      boolean special = distance != null ? (environment.getAt(distance, expr.name.lexeme) != null
       || globals.getAt(distance, expr.name.lexeme) != null) : (environment.get(expr.name) != null || globals.get(expr.name) != null);

      if(special){
        
        switch (expr.type){
          case ADD:{
            if(leftIsDouble && rightIsDouble){
              right = leftD + rightD;
            }
            else if(leftIsDouble && !rightIsDouble){
              right = leftD + rightS;
            }
            else if(!leftIsDouble && rightIsDouble){
              right = leftS + rightD;
            }
            else{
              right = leftS + rightS;
            }
            break;
          }
          case INCREMENT:{
            if(!(leftIsDouble)){
              throw new RuntimeError(new Token(TokenType.INCREMENT, left.toString(), left, 0), "Can only increment numbers");
            }
            right = (double)leftD + 1;
            break;
          }
          case SUBTRACT:{
            if(!(leftIsDouble && rightIsDouble)){
              throw new RuntimeError(new Token(TokenType.INCREMENT, left.toString(), left, 0), "Can only subtract numbers");
            }
            right = (double)(leftD - rightD);
            break;
          }
          case DECREMENT:{
            if(!(leftIsDouble)){
              throw new RuntimeError(new Token(TokenType.INCREMENT, left.toString(), left, 0), "Can only increment numbers");
            }
            right = (double)leftD - 1;
            break;
          }
          case MULTIPLY:{
            if(!(leftIsDouble && rightIsDouble)){
              throw new RuntimeError(new Token(TokenType.INCREMENT, left.toString(), left, 0), "Can only multiply numbers");
            }
            right = (double)(leftD * rightD);
            break;
          }
          case POWER:{
            if(!(leftIsDouble && rightIsDouble)){
              throw new RuntimeError(new Token(TokenType.INCREMENT, left.toString(), left, 0), "Can only exponent numbers");
            }
            right = (double)(Math.pow(leftD, rightD));
            break;
          }
          case DIVIDE:{
            if(!(leftIsDouble && rightIsDouble)){
              throw new RuntimeError(new Token(TokenType.INCREMENT, left.toString(), left, 0), "Can only divide numbers");
            }
            right = (double)(leftD / rightD);
            break;
          }
          default:
            break;
        }
      }
    
    }
    
    if(distance != null){
      environment.assignAt(distance, expr.name, right, modifiers, type);
    } else {
      globals.assign(expr.name, right);
    }

    return right;
  }

  public boolean notSpecialAssignment(AssignType type){
    return type != AssignType.INCREMENT && type != AssignType.DECREMENT;
  }

  public boolean validAssignment(Object left, Object right){
    LoxType leftType = new LoxType(left);
    LoxType rightType = new LoxType(right);
    return leftType.matches(rightType);
  }

  @Override
  public Void visitBlockStmt(Block stmt) {
    executeBlock(stmt.statements, new Environment(environment, this));
    return null;
  }

  void executeBlock(List<Stmt> statements, Environment environment){
    Environment previous = this.environment;
    try{
      this.environment = environment;

      for(Stmt stmt : statements){
        execute(stmt);
      }
    }finally{
       this.environment = previous;
     }
  }

  @Override
  public Void visitIfStmt(If stmt) {
    if(isTruthy(evaluate(stmt.condition))){
      execute(stmt.thenBranch);
    }
    else if(stmt.elseBranch != null){
      execute(stmt.elseBranch);
    }
    return null;
  }

  @Override
  public Void visitTestStmt(Stmt.Test stmt) {
    Object name = evaluate(stmt.name);
    if(!(name instanceof String)) throw new RuntimeError(new Token(TokenType.OBJECT, name.toString(), name, 0), "Test name must be a string");
    try{
      execute(stmt.body);
    }
    catch (com.nix.lox.Expect exVal){
        if((Boolean)exVal.value){
          System.out.println("Test '" + name + "' passed");
        }
        else{
          System.out.println("Test '" + name + "' failed");
        }
        return null;
    }
    return null;
  }

  @Override
  public Object visitLogicalExpr(Logical expr) {
    Object left = evaluate(expr.left);

    if(expr.operator.type == TokenType.OR){
      if(isTruthy(left)) return left;
    }else if (expr.operator.type == TokenType.AND){
      if(!isTruthy(left)) return left;
    }
    else if (expr.operator.type == TokenType.IS){
      LoxType leftType = new LoxType(left);
      Object right = evaluate(expr.right);
      LoxType rightType = new LoxType(right);
      if(right instanceof LoxType) {
        rightType = (LoxType)right;
      }
      return leftType.matches(rightType);
    }

    return evaluate(expr.right);
  }

  @Override
  public Void visitWhileStmt(While stmt) {
    while(isTruthy(evaluate(stmt.condition))){
      try{
        execute(stmt.body);
      }
      catch (com.nix.lox.Break b){
        break;
      }
    }

    return null;
  }

  @Override
  public Void visitWhenStmt(Stmt.When stmt) {
    while(!isTruthy(evaluate(stmt.condition))){
      execute(stmt.thenBranch);
    }
    if(stmt.finallyBranch != null){
      execute(stmt.finallyBranch);
    }
    return null;
  }

  @Override
  public Object visitCallExpr(Call expr) {
    Object callee = evaluate(expr.callee);
    if(expr.nullCheck){
      if(callee == null){
        return null;
      }
    }

    List<Object> arguments = new ArrayList<>();
    for(Expr argument : expr.arguments){
      arguments.add(evaluate(argument));
    }

    List<LoxClass> generics = new ArrayList<>();
    for(Token generic : expr.templates){
      Object value = lookUpVariable(generic, expr);
      if(value instanceof LoxClass){
        generics.add((LoxClass)value);
      }
      else{
        throw new RuntimeError(generic, "Expected class, got '" + value + "' instead");
      }
    }

    if(!(callee instanceof LoxCallable)){
      throw new RuntimeError(expr.paren, "Can only call functions and class constructors, not '" + callee + "'");
    }

    // if(callee instanceof LoxInstance) {
    //   LoxInstance instance = (LoxInstance)callee;
    //   for(int i = 0; i < instance.klass.fields.keySet().size(); i++) {
    //     String key = (String)instance.klass.fields.keySet().toArray()[i];
    //     Object value = instance.klass.fields.get(key).value;
    //   }
    // }

    LoxCallable function = (LoxCallable)callee;
    if(arguments.size() != function.arity()){
      throw new RuntimeError(expr.paren, "Expected " +
          function.arity() + " arguments but got " +
          arguments.size() + ".");
    }

    if(function instanceof LoxFunction) {
      return ((LoxFunction)function).callFunction(this, arguments, false, generics);
    }

    return function.call(this, arguments, generics);
  }

  @Override
  public Void visitFunctionStmt(Function stmt) {
    LoxFunction function = new LoxFunction(stmt, environment, false, stmt.returnType, stmt.modifiers);
    if(stmt.extClass == null) {
      environment.define(function.getName(), function, function.modifiers);
    }
    else{
      Object var = environment.get(stmt.extClass);
      if(var != null){
        if(var instanceof LoxClass){
          ((LoxClass)var).methods.put(function.getName(), function);
        }
        else{
          throw new RuntimeError(stmt.name, "Cannot extend non-class '" + stmt.extClass.lexeme + "'");
        }
      }
      else{
        throw new RuntimeError(stmt.name, "Cannot extend unknown object '" + stmt.extClass.lexeme + "'");
      }
    }

    return null;
  }

  @Override
  public Void visitReturnStmt(Return stmt) {
    Object value = null;
    if(stmt.value != null) value = evaluate(stmt.value);

    throw new com.nix.lox.Return(value);
  }

  public void checkParameters(Stmt.Function func, Stmt.Function template) {
    if(func.params.size() != template.params.size()){
      throw new RuntimeError(func.name, "Function '"+func.name.lexeme+"' must match the method signature of interface '"+template.name.lexeme+"', method: '"+template.name.lexeme+"'");
    }
    for(int i = 0; i < func.params.size(); i++) {
      if(!(func.params.get(i).type.matches(template.params.get(i).type))){
        throw new RuntimeError(func.name, "Function '"+func.name.lexeme+"' must match the type signature of parameter '"+template.params.get(i).name.lexeme+"', "
        + "method: '"+template.name.lexeme+"', " + "expected: " + template.params.get(i).type.name + ", got: " + func.params.get(i).type.name);
      }
    }
  }

  @Override
  public Void visitClassStmt(Class stmt) {
    Object superclass = null;
    if(stmt.superclass != null){
      superclass = evaluate(stmt.superclass);
      if(!(superclass instanceof LoxClass)){
        throw new RuntimeError(stmt.superclass.name, "Superclass must be a class");
      }
      else if(stmt.superclass.name.lexeme.equals("Object")){
        throw new RuntimeError(stmt.superclass.name, "Classes automatically inherit from Object");
      }
    }

  
    environment.define(stmt.name.lexeme, stmt.name, Modifiers.empty());

    environment = new Environment(environment, this);
    environment.define("super", superclass, Modifiers.empty());
    for(Token type : stmt.templates) {
      environment.define(type.lexeme, new LoxObject(environment, current, type.lexeme, null), Modifiers.empty(), new LoxType("any", TypeEnum.ANY));
    }


    Map<String, LoxFunction> methods = new HashMap<>();
    Map<String, Field> fields = new HashMap<>();
    for(Stmt.Function method : stmt.methods) {
      Stmt.Function func = findFunctionOnInterfaces(stmt.interfase, method.name.lexeme);
      if(!method.hasBody){
        if(func != null){
          if(!func.hasBody){
            throw new RuntimeError(stmt.name, "Can't call abstract method '"+func.name.lexeme+"' from object '"+stmt.name.lexeme+"' because the matching interface function does not declare a body");
          }
          if(func.modifiers.mismatch(method.modifiers)){
            throw new RuntimeError(stmt.name, "Class '"+ stmt.name.lexeme +"' must match the method signature of interface '"+func.name.lexeme+"', method: '"+func.name.lexeme+"'");
          }
          if(func != null) checkParameters(method, func);
          if(method.returnType.mismatch(func.returnType)){
                throw new RuntimeError(stmt.name, "Class '"+ stmt.name.lexeme +"' must match the return signature of interface '"+func.name.lexeme+"', method: '"+func.name.lexeme+"'");
          }
          //method.body = func.body;
        }
        else{
          throw new RuntimeError(stmt.name, "Can't call abstract method '"+method.name.lexeme+"' from object '"+stmt.name.lexeme+"' because there no matching interface functions");
        }
      }
      
      LoxFunction function = new LoxFunction(method, environment, false, method.returnType, method.modifiers);
      methods.put(method.name.lexeme, function);
    }

    

    for (Stmt.Var var : stmt.variables) {
      Object value = evaluate(var.initializer);
      if(new LoxType(value).mismatch(var.type)){
        throw new RuntimeError(var.name, "Variable '"+var.name.lexeme+"' must be of type '"+var.type.name+"', got '"+new LoxType(value).name+"'");
      }
      Field f = new Field(value, var.modifiers, var.type);
      checkModifiers(f, var.name.lexeme);
      fields.put(var.name.lexeme, f);
    }

    for (Stmt.Property var : stmt.props) {
      LoxProperty property = property(var);
      Field f = new Field(property, var.modifiers, var.type);
      checkModifiers(f, var.name.lexeme);
      fields.put(var.name.lexeme, f);
    }

    
    

    if(stmt.interfase != null) {
      for(Token interfaseToken : stmt.interfase) {
        Object interfaseValue = null;
        if(stmt.interfase != null){
          interfaseValue = environment.get(interfaseToken);
        } 
        LoxInterface inter = null;
        if(interfaseValue != null){
          if(interfaseValue instanceof LoxInterface) {
            inter = (LoxInterface)interfaseValue;
          }
        }

        if(inter != null){
          for(Stmt.Function name : inter.methods.values()){
            if(!methods.containsKey(name.name.lexeme)){
              throw new RuntimeError(stmt.name, "Class '"+ stmt.name.lexeme +"' must implement all methods of interface '"+inter.name+"', method: '"+name.name.lexeme+"'");
            }
            else{
              LoxFunction function = methods.get(name.name.lexeme);
              if(function.modifiers.mismatch(name.modifiers)){
                throw new RuntimeError(stmt.name, "Class '"+ stmt.name.lexeme +"' must match the method signature of interface '"+inter.name+"', method: '"+name.name.lexeme+"'");
              }
              //params
              if(function.arity() != name.params.size()){
                throw new RuntimeError(stmt.name, "Class '"+ stmt.name.lexeme +"' must match the method signature of interface '"+inter.name+"', method: '"+name.name.lexeme+"'");
              }
            }
          }
          for(Stmt.Var name : inter.fields.values()){
            if(!fields.containsKey(name.name.lexeme)){
              throw new RuntimeError(stmt.name, "Class '"+ stmt.name.lexeme +"' must implement all fields of interface '"+inter.name+"', field: '"+name.name.lexeme+"'");
            }
            else{
              Field field = fields.get(name.name.lexeme);
              if(field.modifiers.mismatch(name.modifiers)){
                throw new RuntimeError(stmt.name, "Class '"+ stmt.name.lexeme +"' must match the field signature of interface '"+inter.name+"', field: '"+name.name.lexeme+"'");
              }
            }
          }
        }
      }
    }
    
    

    LoxClass klass = new LoxClass(environment, stmt.name.lexeme, (LoxClass)superclass, methods, fields, this);
    klass.templates = stmt.templates;

    if(environment.enclosing != null){
      environment = environment.enclosing;
    }
    
    
    if(klass.superClass == null){
      klass.superClass = new LoxObject(environment, this, stmt.name.lexeme, klass);
    }

    environment.assign(stmt.name, klass);
    return null;
  }

  public Stmt.Function findFunctionOnInterfaces(List<Token> interfaces, String name){
    if(interfaces == null) return null; 
    for(Token interfaseToken : interfaces) {
      Object interfaseValue = null;
      if(interfaces != null){
        interfaseValue = environment.get(interfaseToken);
      } 
      LoxInterface inter = null;
      if(interfaseValue != null){
        if(interfaseValue instanceof LoxInterface) {
          inter = (LoxInterface)interfaseValue;
        }
      }

      if(inter != null){
        for(Stmt.Function func : inter.methods.values()){
          if(func.name.lexeme.equals(name)){
            return func;
          }
        }
      }
    }
    return null;
  }

  @Override
  public Void visitInterfaceStmt(Stmt.Interface inter){
    environment.define(inter.name.lexeme, inter.name, Modifiers.empty());

    environment = new Environment(environment, this);

    Map<String, Stmt.Function> methods = new HashMap<>();
    Map<String, Stmt.Var> fields = new HashMap<>();
    for(Stmt.Function method : inter.methods) {
      methods.put(method.name.lexeme, method);
      //System.out.println("Interface: " + inter.name.lexeme + ", Method: " + method.name.lexeme + ", isStatic: " + method.isStatic + ", isConstant: " + method.isConstant);
    }
    for (Stmt.Var var : inter.variables) {
      fields.put(var.name.lexeme, var);
      //System.out.println("Interface: " + inter.name.lexeme + ", Var: " + var.name.lexeme + ", isStatic: " + var.isStatic + ", isConstant: " + var.isConstant);
    }

    LoxInterface i = new LoxInterface(inter.name.lexeme, methods, fields);

    if(environment.enclosing != null){
      environment = environment.enclosing;
    }

    environment.assign(inter.name, i);

    return null;
  }

  @Override
  public Void visitEnumStmt(Stmt.Enum stmt){
    environment.define(stmt.name.lexeme, stmt.name, Modifiers.empty());

    environment = new Environment(environment, this);

    Map<Token, LoxEnum.Element> elements = new HashMap<>();

    for(LoxEnum.Element e : stmt.elements) {
      elements.put(e.name, e);
    }

    LoxEnum i = new LoxEnum(stmt.name, elements);

    if(environment.enclosing != null){
      environment = environment.enclosing;
    }

    environment.assign(stmt.name, i);

    return null;
  }

  @Override
  public Void visitSwitchStmt(Switch stmt) {
    Object value = evaluate(stmt.value);
    for(int i = 0; i < stmt.cases.size(); i++){
      Stmt.Case c = stmt.cases.get(i);
      if(isEqual(value, evaluate(c.value))){
        execute(c.body);
        break;
      }
      else{
        if(i == stmt.cases.size()-1){
          if(stmt.defaultCase != null){
            execute(stmt.defaultCase.body);
          }
        }
      }
    }
    return null;
  }

  private void defineNativeClasses() {
    defineSystem();
    defineObject();
    defineMath();
    defineList();
    defineMap();
  }

  private void defineMath() {
    environment.define("Math", null, Modifiers.empty());

    LoxMath math = new LoxMath(environment, this, null);
    environment.assign(new Token(TokenType.CLASS, "Math", null, -1), math);
  }

   private void defineList() {
    environment.define("List", null, Modifiers.empty());

    LoxList list = new LoxList(environment, this, null);
    environment.assign(new Token(TokenType.CLASS, "List", null, -1), list);
  }

  private void defineMap() {
    environment.define("Map", null, Modifiers.empty());

    LoxMap Map = new LoxMap(environment, this, null);
    environment.assign(new Token(TokenType.CLASS, "Map", null, -1), Map);
  }

  public void defineSystem(){
    environment.define("System", null, Modifiers.empty());

    LoxSystem system = new LoxSystem(environment, this, null);
    environment.assign(new Token(TokenType.CLASS, "System", null, -1), system);
  }

  public void defineObject(){
    environment.define("Object", null, Modifiers.empty());
    LoxObject object = new LoxObject(environment, this, "Object", null);
    environment.assign(new Token(TokenType.CLASS, "Object", null, -1), object);
  }

  @Override
  public Object visitGetExpr(Get expr) {
    Object object = evaluate(expr.object);
    if(object instanceof LoxInstance){
      return ((LoxInstance)object).get(expr.name, false);
    }

    throw new RuntimeError(expr.name, "Only instances have properties");
  }

  @Override
  public Object visitCoalesceExpr(Expr.Coalesce expr){
    Object object = evaluate(expr.object);
    if(object instanceof LoxInstance){
      return ((LoxInstance)object).get(expr.name, false);
    }
    else if(object instanceof LoxClass){
      return ((LoxClass)object).findMethod(expr.name.lexeme, true);
    }
    else if(object == null){
      return null;
    }
    throw new RuntimeError(expr.name, "Only instances have properties");
  }

  @Override
  public Object visitSetExpr(Set expr) {
    Object object = evaluate(expr.object);

    if(!(object instanceof LoxInstance)){
      if(object instanceof LoxClass){
        Object value = evaluate(expr.value);
        ((LoxClass)object).set(expr.name.lexeme, value);
        return value;
      }
      else{
        throw new RuntimeError(expr.name, "Only instances and objects have fields");
      } 
    }

    Object value = evaluate(expr.value);
    ((LoxInstance)object).set(expr.name, value);
    return value;
  }

  @Override
  public Object visitThisExpr(This expr) {
    return lookUpVariable(expr.keyword, expr);
  }

  @Override
  public Object visitValueExpr(Value expr) {
    return lookUpVariable(expr.keyword, expr);
  }

  @Override
  public Object visitSuperExpr(Super expr) {
    int distance = locals.get(expr.keyword);
    LoxClass superclass = (LoxClass)environment.getAt(distance, "super");

    LoxInstance object = (LoxInstance)environment.getAt(distance-1, "this");

    LoxFunction method = superclass.findMethod(expr.method.lexeme, false);

    if(method == null){
      throw new RuntimeError(expr.method,
          "Undefined property '" + expr.method.lexeme + "'.");
    }

    return method.bind(object, this);
  }



  @Override
  public Object visitGetStaticExpr(GetStatic expr) {
    Object object = evaluate(expr.object);
    if(object instanceof LoxClass){
      if(((LoxClass)object).findField(expr.name.lexeme, true) != null){
        Object val = ((LoxClass)object).findField(expr.name.lexeme, true).value;
        if(val instanceof LoxProperty) {
          return ((LoxProperty)val).get(this);
        }
        return val;
      }
      else{
        return ((LoxClass)object).findMethod(expr.name.lexeme, true);
      }
    }
    else if(object instanceof LoxEnum) {
      return ((LoxEnum)object).getValue(expr.name);
    }

    throw new RuntimeError(expr.name, "Can only get shared values from a class or enum");
  }



  @Override
  public Object visitNewExpr(New expr) {
    return null;
  }



  @Override
  public Void visitExpectStmt(Expect stmt) {
    Object value = null;
    if(stmt.value != null) value = evaluate(stmt.value);

    if(!(value instanceof Boolean)) throw new RuntimeError(stmt.keyword, "Expected boolean value for test");
  
    throw new com.nix.lox.Expect(value);
  }

  @Override
  public Void visitBreakStmt(Break stmt) {
    throw new com.nix.lox.Break();
  }

  @Override
  public Void visitContinueStmt(Stmt.Continue stmt) {
    throw new com.nix.lox.Continue();
  }

  @Override
  public Void visitModuleStmt(Stmt.Module stmt) {
    return null;
  }

  @Override
  public Void visitGetFileStmt(GetFile stmt) {
    return null;
  }



  @Override
  public Void visitCaseStmt(Case stmt) {
    return null;
  }



  @Override
  public Object visitTypeofExpr(Expr.Typeof stmt) {
    Object value = evaluate(stmt.value);
    return new LoxType(value);
  }



  @Override
  public Object visitAnonymousFunctionExpr(Expr.AnonymousFunction expr) {
    environment = new Environment(environment, this);
    LoxFunction lf = new LoxFunction(expr, environment, expr.returnType, new Modifiers(TokenType.ANONYMOUS));
    if(environment.enclosing != null) {
      environment = environment.enclosing;
    }
    return lf;
  }

  public String UpperFirstLetter(String s) {
      return s.substring(0, 1).toUpperCase() + s.substring(1);
  }

  public Object callCastMethod(LoxInstance instance, LoxType type, Token operator){
    String typeNameCamel = UpperFirstLetter(type.name.contains("func") ? "func" : type.name);
    String methodName = "to" + typeNameCamel;
    methodName = methodName.replace("[]", "Array");
    LoxFunction method = instance.klass.findMethod(methodName, false);
    if(method == null) {
      throw new RuntimeError(operator, "Cannot cast '" + instance.klass.name + "' to '" + type + "' because no cast method '" + methodName + "' was found");
    }
    else if(method.modifiers.not(TokenType.CAST)){
      throw new RuntimeError(operator, "Cannot cast '" + instance.klass.name + "' to '" + type + "' because the method '" + methodName + "' is not marked as a cast method");
    }
    else if(method.returnType.mismatch(type)){
      throw new RuntimeError(operator, "Cannot cast '" + instance.klass.name + "' to '" + type + "' because the cast method '" + methodName + "' does not return the correct type");
    }
    else if(method.declaration.params.size() > 0) {
      throw new RuntimeError(operator, "Cannot cast '" + instance.klass.name + "' to '" + type + "' because the cast method '" + methodName + "' has parameters, which is not allowed");
    }
    else{
      return method.bind(instance, this).callFunction(this, new ArrayList<>(), false, new ArrayList<>());
    }
  }

  @Override
  public Object visitCastExpr(Cast expr) {
    Object castee = evaluate(expr.value);
    LoxType original = new LoxType(castee);
    Object typeObject = evaluate(expr.castType);
    if(!(typeObject instanceof LoxType)) throw new RuntimeError(expr.operator, "Cannot cast '" + original + "' to '" + typeObject + "'");
    LoxType type = (LoxType)typeObject;

    if(original.isObject()) {
      if(castee instanceof LoxInstance) {
        LoxInstance instance = (LoxInstance)castee;
        return callCastMethod(instance, type, expr.operator);
      }
    }
    else{
      try{
        switch(type.type) {
          case STRING:
            return castee.toString();
          case NUMBER: {
              if(castee instanceof String) {
                return Double.parseDouble((String)castee);
              }
              return (double)castee;
            }
          case BOOLEAN: {
              if(castee instanceof String) {
                return Boolean.parseBoolean((String)castee);
              }
              return (boolean)castee;
            }
            
          case VOID:
            return null;
          default:
            break;
        }
      }
      catch (Exception e) {
        throw new RuntimeError(expr.operator, "Cannot cast '" + original + "' to '" + type + "'");
      }
    }
    throw new RuntimeError(expr.operator, "Cannot cast '" + original + "' to '" + type + "'");
  }

  public LoxProperty property(Property stmt) {
    LoxFunction getter = new LoxFunction(stmt.get, environment, false, stmt.get.returnType, stmt.get.modifiers);
    LoxFunction setter = null;
    Object value = defaultValue(stmt.type);
    if(stmt.set != null){
      setter = new LoxFunction(stmt.set, environment, false, stmt.set.returnType, stmt.set.modifiers);
    } 

    LoxProperty property = new LoxProperty(stmt.name, stmt.modifiers, stmt.type, getter, setter);
    property.value = value;

    return property;
  }

  @Override
  public Void visitPropertyStmt(Property stmt) {
    LoxFunction getter = new LoxFunction(stmt.get, environment, false, stmt.get.returnType, stmt.get.modifiers);
    LoxFunction setter = null;
    Object value = defaultValue(stmt.type);
    if(stmt.set != null){
      setter = new LoxFunction(stmt.set, environment, false, stmt.set.returnType, stmt.set.modifiers);
    } 

    LoxProperty property = new LoxProperty(stmt.name, stmt.modifiers, stmt.type, getter, setter);
    property.value = value;

    environment.define(stmt.name.lexeme, property, stmt.modifiers, new LoxType(property));
    return null;
  }



  private Object defaultValue(LoxType type) {
    if(type.name.contains("[]")) {
      return new LoxArray(type, 0);
    }
    switch(type.type) {
      case STRING:
        return "";
      case NUMBER:
        return 0.0;
      case BOOLEAN:
        return false;
      case VOID:
        return null;
      default:
        return null;
    }
  }



  @Override
  public Object visitSetAssignExpr(SetAssign expr) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'visitSetAssignExpr'");
  }



  @Override
  public Object visitArrayExpr(Array expr) {
    LoxType type = expr.type != null ? expr.type : new LoxType(evaluate(expr.values.get(0)));
    Object defVal = defaultValue(type);
    type.name+="[]";
    int size = 0;
    if(expr.size != null) {
      size = (int)((double)evaluate(expr.size));
    }
    else{
      size = expr.values.size();
    }
    LoxArray array = new LoxArray(type, size);

    List<Object> values = new ArrayList<>();
    if(expr.size == null) {
      for(int i = 0; i < expr.values.size(); i++) {
          values.add(evaluate(expr.values.get(i)));
      }
    }
    else {
      for(int i = 0; i < size; i++) {
        values.add(defVal);
      }
    }  

    for(int i = 0; i < values.size(); i++) {
      array.set(i, values.get(i));
    }

    return array;
  }

  public Object callMethodOnObject(Token name, String methodName, List<Object> args, Object obj, boolean operator) {
    if(obj instanceof LoxInstance) {
      LoxFunction method = ((LoxInstance)obj).klass.findMethod(methodName, false);
      if(method != null) {
        return method.bind((LoxInstance)obj, this).callFunction(this, args, operator, new ArrayList<>());
      }
    }
    else if(obj instanceof LoxClass) {
      LoxFunction method = ((LoxClass)obj).findMethod(methodName, false);
      if(method != null) {
        return method.callFunction(this, args, operator, new ArrayList<>(0));
      }
    }
    throw new RuntimeError(name, "Object of type '" + new LoxType(obj).name + "' does not define method '" + methodName + "'");
  }

  @Override
  public Object visitGetIndexExpr(GetIndex expr) {
    Object arrayObj  = evaluate(expr.object);
    Object indexObj = evaluate(expr.index);
    if(!(indexObj instanceof Double)) {
      throw new RuntimeError(expr.name, "index operator can only take a number");
    }
    double index = Math.round((double)indexObj);
    if(!(arrayObj instanceof LoxArray)) {
      if(arrayObj instanceof String) {
        return ((String)arrayObj).charAt((int)index);
      }
      else{
        ArrayList<Object> args = new ArrayList<>();
        args.add(index);
        return callMethodOnObject(expr.name, "getAt", args, arrayObj, true);
      }
    }
    
    LoxArray array = (LoxArray)arrayObj;
    return array.get((int)index);
  }



  @Override
  public Object visitSetIndexExpr(SetIndex expr) {
    Object arrayObj  = evaluate(expr.object);
    Object indexObj = evaluate(expr.index);
    Object valueObj = evaluate(expr.value);
    if(!(indexObj instanceof Double)) {
      if(indexObj instanceof Integer) {
        indexObj = (double)((int)indexObj);
      }
      else throw new RuntimeError(expr.name, "Can only set number index of an array");
    }
    double index = Math.round((double)indexObj);
    if(!(arrayObj instanceof LoxArray)) {
      ArrayList<Object> args = new ArrayList<>();
      args.add(index);
      args.add(valueObj);
      return callMethodOnObject(expr.name, "setAt", args, arrayObj, true);
    }
    
    LoxArray array = (LoxArray)arrayObj;

    array.set((int)index, valueObj);
    return valueObj;
  }



  @Override
  public Object visitLengthExpr(Length expr) {
    Object obj = evaluate(expr.value);
    if(obj instanceof String) {
      return ((String)obj).length();
    }
    else if(obj instanceof LoxArray) {
      return ((LoxArray)obj).getSize();
    }
    else {
      if(obj instanceof LoxInstance) {
        LoxInstance instance = (LoxInstance)obj;
        LoxFunction method = instance.klass.findMethod("getSize", false);
        if(method != null) {
          if(method.returnType.type != TypeEnum.NUMBER) {
            throw new RuntimeError(expr.name, "Object of type '" + new LoxType(obj).name + "' does not have a length");
          }
          return method.bind(instance, this).callFunction(this, new ArrayList<>(), false, new ArrayList<>());
        }
      }
      throw new RuntimeError(expr.name, "Object of type '" + new LoxType(obj).name + "' does not have a length");
    }
  }



  @Override
  public Object visitTernaryExpr(Ternary expr) {
    Object condition = evaluate(expr.condition);
    if(isTruthy(condition)) {
      return evaluate(expr.thenBranch);
    }
    else {
      return evaluate(expr.elseBranch);
    }
  }



  @Override
  public Void visitTryStmt(Try stmt) {
    try{
      execute(stmt.tryBranch);
    }
    catch (com.nix.lox.Break b){
      throw b;
    }
    catch (com.nix.lox.Continue c){
      throw c;
    }
    catch (com.nix.lox.Return r){
      throw r;
    }
    catch (com.nix.lox.Expect e){
      throw e;
    }
    catch (com.nix.lox.RuntimeError e) {
      if(stmt.catchBranch != null){
        environment = new Environment(environment, this);
        environment.define(stmt.exName.lexeme, e.getMessage(), new Modifiers(TokenType.CONST));
        executeBlock(stmt.catchBranch, environment);
        environment = environment.enclosing;
      }
    }
    return null;
  }



  @Override
  public Object visitTypeExpr(Type expr) {
    return expr.type;
  }

  
}