package com.nix.lox;

import java.util.List;

import com.nix.lox.LoxType.TypeEnum;

public class LoxFunction implements LoxCallable{
  public final Stmt.Function declaration;
  private Environment closure;
  private final boolean isInitializer;
  private final boolean isNative;
  private final LoxCallable callable;
  final Modifiers modifiers;
  public boolean global = true;
  public LoxType returnType;

  LoxFunction(Stmt.Function declaration, Environment environment, boolean isInitializer, LoxType returnType, Modifiers modifiers) {
    this.declaration = declaration;
    this.closure = environment;
    this.isInitializer = isInitializer;
    this.isNative = false;
    this.callable = null;
    this.modifiers = modifiers;
    this.returnType = returnType;
  }

  public LoxFunction(LoxCallable loxCallable, Environment environment, boolean isInitializer, LoxType returnType, Modifiers modifiers) {
    this.declaration = null;
    this.closure = environment;
    this.isInitializer = isInitializer;
    this.isNative = true;
    this.callable = loxCallable;
    this.modifiers = modifiers;
    this.returnType = returnType;
  }

  public LoxFunction(Expr.AnonymousFunction body, Environment environment, LoxType rType, Modifiers modifiers) {
    declaration = new Stmt.Function(new Token(TokenType.ANONYMOUS, "null", null, 0), null, body.params, body.body, modifiers, null, rType);
    this.closure = environment;
    this.isInitializer = false;
    this.isNative = false;
    this.callable = null;
    this.modifiers = modifiers;
    this.returnType = rType;
  }

  LoxFunction bind(LoxInstance instance, Interpreter in){
    Environment environment = closure;
    environment.define("this", instance, modifiers);
    if(!isNative) return new LoxFunction(declaration, environment, isInitializer, returnType, modifiers);
    else return new LoxFunction(callable, environment, isInitializer, returnType, modifiers);
  }

  public void define(String name, Object value, Modifiers modifiers, Interpreter in){
    closure = new Environment(closure, in);
    closure.define(name, value, modifiers);
//    if(closure.enclosing != null) closure = closure.enclosing;
  }

  @Override
  public int arity() {
    return isNative ? callable.arity() : declaration.params.size();
  }

  public boolean inEnv(Interpreter intp){
    if(declaration == null) return false;
    for(int i = 0; i < intp.environment.values.size(); i++){
      if(intp.environment.values.keySet().toArray()[i].equals(declaration.name.lexeme)){
        return true;
      }
    }
    return false;
  }

  public Object callFunction(Interpreter interpreter, List<Object> arguments, boolean operatorCall, List<LoxClass> templates) {
    boolean isoperator = modifiers.contains(TokenType.OPERATOR);
    if(isoperator && !operatorCall) {
      throw new RuntimeError(declaration.name, "Operator method '" + declaration.name.lexeme + "' must be called by an operator");
    }
    else if (!isoperator && operatorCall) {
      throw new RuntimeError(declaration.name, "Method '" + declaration.name.lexeme + "' must be an operator method");
    }
    return call(interpreter, arguments, templates);
  }

  public String getName(){
    String types = "(";
    for(int i = 0; i < declaration.params.size(); i++){
      types += declaration.params.get(i).type.name;
      if(i != declaration.params.size() - 1) types += ", ";
    }
    types += ")";
    return declaration.name.lexeme;// + types;
  }

  public void checkParameters(List<Object> arguments){
    for(int i = 0; i < arguments.size(); i++) {
      LoxType argType = new LoxType(arguments.get(i));
      LoxType paramType = declaration.params.get(i).type;
      if(argType.matches(paramType)) {
        continue;
      }
      else{
        throw new RuntimeError(declaration.name, "Expected type '" + paramType + "' for parameter '" + declaration.params.get(i).name.lexeme + "' of function '" + declaration.name.lexeme + "' but got type '" + argType + "' instead");
      }
    }
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> arguments, List<LoxClass> templates) {
    

    global = inEnv(interpreter);
    if(isNative){
      return callable.call(interpreter, arguments, templates);
    }
    if(global){
      if(!declaration.hasBody){
        String parameString = "";
        for(int i = 0; i < declaration.params.size(); i++){
          parameString += declaration.params.get(i).type;
          if(i != declaration.params.size() - 1) parameString += ", ";
        }
        System.out.println("[Warning] Abstract Function '" + declaration.name.lexeme + "("+parameString+")' called from global context");
        return null;
      }
    }

    checkParameters(arguments);
    
    Environment environment = new Environment(closure, interpreter);
    for (int i = 0; i < declaration.params.size(); i++) {
      environment.define(declaration.params.get(i).name.lexeme, arguments.get(i), new Modifiers());
    }

    try{
      interpreter.executeBlock(declaration.body, environment);
    } catch (Return returnValue){
      if(!(new LoxType(returnValue.value).matches(returnType))) throw new RuntimeError(declaration.name, "Expected type '" + returnType + "' for function '" + declaration.name.lexeme + "' return value" );

      if (isInitializer) return closure.getAt(0, "this");

      return returnValue.value;
    }
    if(returnType.type != TypeEnum.VOID) throw new RuntimeError(declaration.name, "Expected type function '" + declaration.name.lexeme + "' to return value of type '" + returnType + "'");

    if(isInitializer) return closure.getAt(0, "this");
    return null;
  }

  @Override
  public String toString() {
    return "<fn " + (declaration == null ? callable.toString() :declaration.name.lexeme) + ">";
  }
}

  
