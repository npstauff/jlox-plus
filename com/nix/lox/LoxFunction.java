package com.nix.lox;

import java.util.List;

import com.nix.lox.LoxType.TypeEnum;

public class LoxFunction implements LoxCallable{
  public final Stmt.Function declaration;
  private final Environment closure;
  private final boolean isInitializer;
  private final boolean isNative;
  private final LoxCallable callable;
  final boolean isStatic;
  final boolean isConstant;
  public boolean global = true;
  public boolean isoperator = false;
  public LoxType returnType;

  LoxFunction(Stmt.Function declaration, Environment environment, boolean isInitializer, boolean isStatic, boolean isConstant, boolean isoperator, LoxType returnType) {
    this.declaration = declaration;
    this.closure = environment;
    this.isInitializer = isInitializer;
    this.isNative = false;
    this.callable = null;
    this.isStatic = isStatic;
    this.isConstant = isConstant;
    this.isoperator = isoperator;
    this.returnType = returnType;
  }

  public LoxFunction(LoxCallable loxCallable, Environment environment, boolean isInitializer, boolean isStatic, boolean isConstant, LoxType returnType) {
    this.declaration = null;
    this.closure = environment;
    this.isInitializer = isInitializer;
    this.isNative = true;
    this.callable = loxCallable;
    this.isStatic = isStatic;
    this.isConstant = isConstant;
    this.returnType = returnType;
  }

  public LoxFunction(LoxCallable loxCallable, Environment environment, boolean isInitializer, boolean isStatic, LoxType returnType) {
    this.declaration = null;
    this.closure = environment;
    this.isInitializer = isInitializer;
    this.isNative = true;
    this.callable = loxCallable;
    this.isStatic = isStatic;
    this.isConstant = false;
    this.returnType = returnType;
  }

  public LoxFunction(LoxCallable loxCallable, Environment environment, boolean isInitializer, LoxType returnType) {
    this.declaration = null;
    this.closure = environment;
    this.isInitializer = isInitializer;
    this.isNative = true;
    this.callable = loxCallable;
    this.isStatic = false;
    this.isConstant = false;
    this.returnType = returnType;
  }

  LoxFunction bind(LoxInstance instance){
    Environment environment = new Environment(closure);
    environment.define("this", instance, false, false, false);
    if(!isNative) return new LoxFunction(declaration, environment, isInitializer, isStatic, isConstant, isoperator, returnType);
    else return new LoxFunction(callable, environment, isInitializer, isStatic, isConstant, returnType);
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

  public Object callFunction(Interpreter interpreter, List<Object> arguments, boolean operatorCall) {
    if(isoperator && !operatorCall) {
      throw new RuntimeError(declaration.name, "Operator method '" + declaration.name.lexeme + "' must be called by an operator");
    }
    else if (!isoperator && operatorCall) {
      throw new RuntimeError(declaration.name, "Method '" + declaration.name.lexeme + "' must be an operator method");
    }
    return call(interpreter, arguments);
  }

  public void checkParameters(List<Object> arguments){
    for(int i = 0; i < arguments.size(); i++) {
      LoxType argType = new LoxType(arguments.get(i));
      LoxType paramType = declaration.params.get(i).type;
      if(argType.matches(paramType)) {
        continue;
      }
      else{
        throw new RuntimeError(declaration.name, "Expected type '" + paramType.type + "' for parameter '" + declaration.params.get(i).name.lexeme + "' of function '" + declaration.name.lexeme + "' but got type '" + argType.type + "' instead");
      }
    }
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> arguments) {
    

    global = inEnv(interpreter);
    if(isNative){
      return callable.call(interpreter, arguments);
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
    
    Environment environment = new Environment(closure);
    for (int i = 0; i < declaration.params.size(); i++) {
      environment.define(declaration.params.get(i).name.lexeme, arguments.get(i), false, false, false);
    }

    try{
      interpreter.executeBlock(declaration.body, environment);
    } catch (Return returnValue){
      if(!(new LoxType(returnValue.value).matches(returnType))) throw new RuntimeError(declaration.name, "Expected type '" + returnType.type + "' for function '" + declaration.name.lexeme + "' return value" );

      if (isInitializer) return closure.getAt(0, "this");

      return returnValue.value;
    }
    if(returnType.type != TypeEnum.VOID) throw new RuntimeError(declaration.name, "Expected type function '" + declaration.name.lexeme + "' to return value of type '" + returnType.type + "'");

    if(isInitializer) return closure.getAt(0, "this");
    return null;
  }

  @Override
  public String toString() {
    return "<fn " + (declaration == null ? callable.toString() :declaration.name.lexeme) + ">";
  }
}

  
