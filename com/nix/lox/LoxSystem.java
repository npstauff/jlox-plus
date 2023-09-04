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
    methods.put("println", debug(environment, true, false));
    methods.put("print", debug(environment, false, false));
    methods.put("cls", cls(environment));
    methods.put("errln", debug(environment, true, true));
    methods.put("err", debug(environment, false, true));
    methods.put("random", random(environment));
    return methods;
  }

  private LoxFunction random(Environment environment) {
    return new LoxFunction(new LoxCallable() {

      @Override
      public int arity() {
        return 3;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        double lower = (double)arguments.get(0);
        double upper = (double)arguments.get(1);
        boolean inclusive = (boolean)arguments.get(2);
        return (double)(Math.random() * (upper - lower)) + lower + (inclusive ? 1 : 0);
      }
      
    }, environment, false, true);
  }

  LoxFunction debug(Environment environment, boolean newline, boolean err){
    return new LoxFunction(new LoxCallable() {

      @Override
      public int arity() {
        return 1;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        if(!err) System.out.print(arguments.get(0) + (newline ? "\n" : ""));
        else System.err.print(arguments.get(0) + (newline ? "\n" : ""));
        return null;
      }
      
    }, environment, false, true);
  }

  LoxFunction cls(Environment environment){
    return new LoxFunction(new LoxCallable() {

      @Override
      public int arity() {
        return 0;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        System.out.print("\033[H\033[2J");  
        System.out.flush();
        return null;
      }
      
    }, environment, false, true);
  }

  @Override
  public void defineFields() {
  }
}