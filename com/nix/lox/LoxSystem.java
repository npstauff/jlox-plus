package com.nix.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nix.lox.Expr.This;

/**
 * LoxObject
 */
public class LoxSystem extends LoxNative{

  //public static LoxSystem system;

  LoxSystem(Environment environment, Interpreter interpreter, LoxClass type){
    super(null, null, null, null, type);
    setDetails("System", new LoxObject(environment, interpreter, "System", this.type), defineFunctions(environment), interpreter);
  }

  public Map<String, LoxFunction> defineFunctions(Environment environment) {
    Map<String, LoxFunction> methods = new HashMap<>();
    methods.put("debug", debug(environment));
    methods.put("random", random(environment));
    return methods;
  }

  private LoxFunction random(Environment environment) {
    return new LoxFunction(new LoxCallable() {

      @Override
      public int arity() {
        return 2;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        double lower = (double)arguments.get(0);
        double upper = (double)arguments.get(1);
        return Math.random()*upper+lower;
      }
      
    }, environment, false, true);
  }

  LoxFunction debug(Environment environment){
    return new LoxFunction(new LoxCallable() {

      @Override
      public int arity() {
        return 0;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        return "debug from system";
      }
      
    }, environment, false);
  }

  @Override
  public void defineFields() {
    put("system", new LoxInstance(this, interpreter), true, false);
  }
}