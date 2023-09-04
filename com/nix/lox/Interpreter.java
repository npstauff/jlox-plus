package com.nix.lox;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.Writer;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sun.misc.Unsafe;

import com.nix.lox.Expr.Assign;
import com.nix.lox.Expr.Binary;
import com.nix.lox.Expr.Call;
import com.nix.lox.Expr.Get;
import com.nix.lox.Expr.GetStatic;
import com.nix.lox.Expr.Grouping;
import com.nix.lox.Expr.Literal;
import com.nix.lox.Expr.Logical;
import com.nix.lox.Expr.New;
import com.nix.lox.Expr.Set;
import com.nix.lox.Expr.Super;
import com.nix.lox.Expr.This;
import com.nix.lox.Expr.Unary;
import com.nix.lox.Expr.Variable;
import com.nix.lox.Expr.Visitor;
import com.nix.lox.Stmt.Block;
import com.nix.lox.Stmt.Class;
import com.nix.lox.Stmt.Expect;
import com.nix.lox.Stmt.Expression;
import com.nix.lox.Stmt.Function;
import com.nix.lox.Stmt.GetFile;
import com.nix.lox.Stmt.If;
import com.nix.lox.Stmt.Return;
import com.nix.lox.Stmt.Var;
import com.nix.lox.Stmt.While;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

  final Environment globals = new Environment();
  Environment environment = globals;
  private final Map<Expr, Integer> locals = new HashMap<>();

  Interpreter(){
    defineNativeFunctions();
    defineNativeClasses();
  }

  

  private void defineNativeFunctions(){
    globals.define("clock", new LoxCallable() {

      @Override
      public int arity() {
        return 0;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        return (double)System.currentTimeMillis()/1000.0;
      }
      
      @Override
      public String toString() { return "<native fun>"; };
    }, true, false, false);

    globals.define("readLine", new LoxCallable() {

      @Override
      public int arity() {
        return 0;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        return scanner.nextLine();
      }
      
    }, true, false, false);

    globals.define("readNum", new LoxCallable() {

      @Override
      public int arity() {
        return 0;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        double d = 0;
        try{
          d = scanner.nextDouble();
        } catch (Exception e){
          throw new RuntimeError(new Token(TokenType.NUMBER, ((Object)d).toString(), d, -1), "input format error, expected type NUMBER");
        }
        return d;
      }
      
    }, true ,false, false);

    globals.define("readBool", new LoxCallable() {

      @Override
      public int arity() {
        return 0;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        boolean b = false;
        try{
          b = scanner.nextBoolean();
        } catch (Exception e){
          throw new RuntimeError(new Token(TokenType.OBJECT, ((Object)b).toString(), b, -1), "input format error, expected type BOOLEAN");
        }
        return b;
      }
      
    }, true, false, false);

    globals.define("scanFile", new LoxCallable() {

      @Override
      public int arity() {
        return 1;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        String text = "";
        String path = (String)arguments.get(0);
        try {
          File f = new File(path);
          java.util.Scanner reader = new java.util.Scanner(f);
          while(reader.hasNextLine()){
            text += reader.nextLine()+"\n";
          }
        } catch (FileNotFoundException e) {
          throw new RuntimeError(new Token(TokenType.OBJECT, "", path, -1), "file at location " + path + " not found");
        }
        
        return text;
      }
      
    }, true, false, false);

    globals.define("print", new LoxCallable() {

      @Override
      public int arity() {
        return 1;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        System.out.print(arguments.get(0));
        return null;
      }
      
    }, true, false, false);

    globals.define("println", new LoxCallable() {

      @Override
      public int arity() {
        return 1;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        System.out.println(arguments.get(0));
        return null;
      }
      
    }, true, false, false);

    globals.define("error", new LoxCallable() {

      @Override
      public int arity() {
        return 1;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        System.err.println(arguments.get(0));
        return null;
      }
      
    }, true, false, false);

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
          else if (left instanceof String && right instanceof String) {
            return (String)left + (String)right;
          }
          else{
            String str = "";
            if(left != null){
              str += left.toString();
            }
            if(right != null){
              str += right.toString();
            }
            return str;
          }

          case SLASH:
          checkNumberOperands(expr.operator, left, right);
          return (double)left / (double)right;
        case STAR:
          checkNumberOperands(expr.operator, left, right);
          return (double)left * (double)right;
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

  void resolve(Expr expr, int depth){
    locals.put(expr, depth);
  }

  private String stringify(Object object) {
    if (object == null) return "nil";

    if (object instanceof Double) {
      String text = object.toString();
      if (text.endsWith(".0")) {
        text = text.substring(0, text.length() - 2);
      }
      return text;
    }

    return object.toString();
  }

  @Override
  public Void visitExpressionStmt(Expression stmt) {
    evaluate(stmt.expression);
    return null;
  }


  @Override
  public Void visitVarStmt(Var stmt) {
    Object value = null;
    if(stmt.initializer != null) {
      value = evaluate(stmt.initializer);
    }
    if(stmt.pointer){
      if(value instanceof LoxObject){
        throw new RuntimeError(new Token(TokenType.VAR, stmt.name.lexeme, value, 0), "Pointer must be reference");
      }
    }
    environment.define(stmt.name.lexeme, value, stmt.isConstant, stmt.isStatic, stmt.pointer);
    return null;
  }

  @Override
  public Object visitVariableExpr(Variable expr) {
    return lookUpVariable(expr.name, expr);
  }

  private Object lookUpVariable(Token name, Expr expr) {
    Integer distance = locals.get(expr);
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
    boolean isConstant = environment.values.get(expr.name.lexeme) != null ? environment.values.get(expr.name.lexeme).constant : false;
    boolean isStatic = environment.values.get(expr.name.lexeme) != null ? environment.values.get(expr.name.lexeme).isstatic : false;
    boolean ptr = environment.values.get(expr.name.lexeme) != null ? environment.values.get(expr.name.lexeme).pointer : false;
    Integer distance = locals.get(expr);

    boolean hasObject = distance != null ? 
      environment.getAt(distance, expr.name.lexeme) != null : environment.get(expr.name) != null;

    if(hasObject){
      boolean leftIsDouble = false;
      boolean rightIsDouble = false;
      Object left = distance != null ? environment.getAt(distance, expr.name.lexeme) : environment.get(expr.name);

      if(!validAssignment(left, right) && (expr.type != AssignType.INCREMENT && expr.type != AssignType.DECREMENT)){
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
          rightS = (String)right;
        }
      }
      if(left instanceof Double){
        leftIsDouble = true;
        leftD = (int)((double)left);
      }
      else{
        leftS = (String)left;
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
        }
      }
    
    }
    
    if(distance != null){
      environment.assignAt(distance, expr.name, right, isConstant, isStatic, ptr);
    } else {
      globals.assign(expr.name, right);
    }

    return right;
  }

  public boolean notSpecialAssignment(AssignType type){
    return type != AssignType.INCREMENT && type != AssignType.DECREMENT;
  }

  public boolean validAssignment(Object left, Object right){
    if((!(left instanceof Double) && !(left instanceof String))
    || (!(right instanceof Double) && !(right instanceof String))){
      return false;
    }
    return true;
  }

  @Override
  public Void visitBlockStmt(Block stmt) {
    executeBlock(stmt.statements, new Environment(environment));
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
          System.out.println("Test " + name + " passed");
        }
        else{
          System.out.println("Test " + name + " failed");
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
    }else{
      if(!isTruthy(left)) return left;
    }

    return evaluate(expr.right);
  }

  @Override
  public Void visitWhileStmt(While stmt) {
    while(isTruthy(evaluate(stmt.condition))){
      execute(stmt.body);
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

    if(!(callee instanceof LoxCallable)){
      throw new RuntimeError(expr.paren, "Can only call functions and class constructors");
    }

    LoxCallable function = (LoxCallable)callee;
    if(arguments.size() != function.arity()){
      throw new RuntimeError(expr.paren, "Expected " +
          function.arity() + " arguments but got " +
          arguments.size() + ".");
    }


    return function.call(this, arguments);
  }

  @Override
  public Void visitFunctionStmt(Function stmt) {
    LoxFunction function = new LoxFunction(stmt, environment, false, stmt.isStatic, stmt.isConstant);
    if(stmt.extClass == null) {
      environment.define(stmt.name.lexeme, function, function.isConstant, function.isStatic, false);
    }
    else{
      Object var = environment.get(stmt.extClass);
      if(var != null){
        if(var instanceof LoxClass){
          ((LoxClass)var).methods.put(stmt.name.lexeme, function);
        }
        else{
          throw new RuntimeError(stmt.name, "Cannot extend non-class");
        }
      }
      else{
        throw new RuntimeError(stmt.name, "Cannot extend non-class");
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

  
    environment.define(stmt.name.lexeme, stmt.name, false, false, false);

    environment = new Environment(environment);
    environment.define("super", superclass, false, false, false);
    


    Map<String, LoxFunction> methods = new HashMap<>();
    Map<String, Field> fields = new HashMap<>();
    for(Stmt.Function method : stmt.methods) {
      LoxFunction function = new LoxFunction(method, environment, false, method.isStatic, method.isConstant);
      methods.put(method.name.lexeme, function);
    }
    for (Stmt.Var var : stmt.variables) {
      Object value = evaluate(var.initializer);
      Field f = new Field(value, var.isConstant, var.isStatic, var.pointer);
      fields.put(var.name.lexeme, f);
    }

    LoxClass klass = new LoxClass(stmt.name.lexeme, (LoxClass)superclass, methods, fields, this);

    if(environment.enclosing != null){
      environment = environment.enclosing;
    }
    
    
    if(klass.superClass == null){
      klass.superClass = new LoxObject(environment, this, stmt.name.lexeme, klass);
    }

    environment.assign(stmt.name, klass);
    return null;
  }

  private void defineNativeClasses() {
    defineSystem();
    defineObject();
    defineGraphics();
    defineMath();
    defineList();
    defineMap();
    defineColor();
  }

  private void defineColor() {
    environment.define("Color", null, false ,false, false);

    LoxColor c = new LoxColor(environment, this, null);
    environment.assign(new Token(TokenType.CLASS, "Color", null, -1), c);
  }

  private void defineMath() {
    environment.define("Math", null, false, false, false);

    LoxMath math = new LoxMath(environment, this, null);
    environment.assign(new Token(TokenType.CLASS, "Math", null, -1), math);
  }

   private void defineList() {
    environment.define("List", null, false, false, false);

    LoxList list = new LoxList(environment, this, null);
    environment.assign(new Token(TokenType.CLASS, "List", null, -1), list);
  }

  private void defineMap() {
    environment.define("Map", null, false, false, false);

    LoxMap Map = new LoxMap(environment, this, null);
    environment.assign(new Token(TokenType.CLASS, "Map", null, -1), Map);
  }

  public void defineSystem(){
    environment.define("System", null, false, false, false);

    LoxSystem system = new LoxSystem(environment, this, null);
    environment.assign(new Token(TokenType.CLASS, "System", null, -1), system);
  }

  public void defineObject(){
    environment.define("Object", null, false, false, false);
    LoxObject object = new LoxObject(environment, this, "Object", null);
    environment.assign(new Token(TokenType.CLASS, "Object", null, -1), object);
  }

  public void defineGraphics(){
    environment.define("Graphics", null, false, false, false);
    LoxGraphics graphics = new LoxGraphics(environment, this, null);
    environment.assign(new Token(TokenType.CLASS, "Graphics", null, -1), graphics);
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
        throw new RuntimeError(expr.name, "Only instances have fields");
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
  public Object visitSuperExpr(Super expr) {
    int distance = locals.get(expr);
    LoxClass superclass = (LoxClass)environment.getAt(distance, "super");

    LoxInstance object = (LoxInstance)environment.getAt(distance-1, "this");

    LoxFunction method = superclass.findMethod(expr.method.lexeme, false);

    if(method == null){
      throw new RuntimeError(expr.method,
          "Undefined property '" + expr.method.lexeme + "'.");
    }

    return method.bind(object);
  }



  @Override
  public Object visitGetStaticExpr(GetStatic expr) {
    Object object = evaluate(expr.object);
    if(object instanceof LoxClass){
      if(((LoxClass)object).findField(expr.name.lexeme, true) != null){
        return ((LoxClass)object).findField(expr.name.lexeme, true);
      }
      else{
        return ((LoxClass)object).findMethod(expr.name.lexeme, true);
      }
    }

    throw new RuntimeError(expr.name, "Can only get public functions from a class");
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
  public Void visitModuleStmt(Stmt.Module stmt) {
    return null;
  }

  @Override
  public Void visitGetFileStmt(GetFile stmt) {
    return null;
  }

  
}