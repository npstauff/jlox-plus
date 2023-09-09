package com.nix.lox;

import java.util.List;

public class LoxFunction implements LoxCallable{
  private final Stmt.Function declaration;
  private final Environment closure;
  private final boolean isInitializer;
  private final boolean isNative;
  private final LoxCallable callable;
  final boolean isStatic;
  final boolean isConstant;
  public boolean global = true;

  LoxFunction(Stmt.Function declaration, Environment environment, boolean isInitializer, boolean isStatic, boolean isConstant) {
    this.declaration = declaration;
    this.closure = environment;
    this.isInitializer = isInitializer;
    this.isNative = false;
    this.callable = null;
    this.isStatic = isStatic;
    this.isConstant = isConstant;
  }

  public LoxFunction(LoxCallable loxCallable, Environment environment, boolean isInitializer, boolean isStatic, boolean isConstant) {
    this.declaration = null;
    this.closure = environment;
    this.isInitializer = isInitializer;
    this.isNative = true;
    this.callable = loxCallable;
    this.isStatic = isStatic;
    this.isConstant = isConstant;
  }

  public LoxFunction(LoxCallable loxCallable, Environment environment, boolean isInitializer, boolean isStatic) {
    this.declaration = null;
    this.closure = environment;
    this.isInitializer = isInitializer;
    this.isNative = true;
    this.callable = loxCallable;
    this.isStatic = isStatic;
    this.isConstant = false;
  }

  public LoxFunction(LoxCallable loxCallable, Environment environment, boolean isInitializer) {
    this.declaration = null;
    this.closure = environment;
    this.isInitializer = isInitializer;
    this.isNative = true;
    this.callable = loxCallable;
    this.isStatic = false;
    this.isConstant = false;
  }

  LoxFunction bind(LoxInstance instance){
    Environment environment = new Environment(closure);
    environment.define("this", instance, false, false, false);
    if(!isNative) return new LoxFunction(declaration, environment, isInitializer, isStatic, isConstant);
    else return new LoxFunction(callable, environment, isInitializer, isStatic, isConstant);
  }

  @Override
  public int arity() {
    return isNative ? callable.arity() : declaration.params.size();
  }

  public boolean inEnv(Interpreter intp){
    for(int i = 0; i < intp.environment.values.size(); i++){
      if(intp.environment.values.keySet().toArray()[i].equals(declaration.name.lexeme)){
        return true;
      }
    }
    return false;
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
          parameString += declaration.params.get(i).lexeme;
          if(i != declaration.params.size() - 1) parameString += ", ";
        }
        System.out.println("[Warning] Abstract Function '" + declaration.name.lexeme + "("+parameString+")' called from global context");
        return null;
      }
    }
    
    Environment environment = new Environment(closure);
    for (int i = 0; i < declaration.params.size(); i++) {
      environment.define(declaration.params.get(i).lexeme, arguments.get(i), false, false, false);
    }

    try{
      interpreter.executeBlock(declaration.body, environment);
    } catch (Return returnValue){
      if (isInitializer) return closure.getAt(0, "this");

      return returnValue.value;
    }

    if(isInitializer) return closure.getAt(0, "this");
    return null;
  }

  @Override
  public String toString() {
    return "<fn " + (declaration == null ? callable.toString() :declaration.name.lexeme) + ">";
  }
}

  
