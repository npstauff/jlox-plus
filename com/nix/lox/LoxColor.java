package com.nix.lox;

import java.util.List;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LoxColor extends LoxNative{
  private List<Object> items = new ArrayList<Object>();


  LoxColor(Environment environment, Interpreter interpreter, LoxClass type){
    super(null, null, null, null, type);
    setDetails("Color", new LoxObject(environment, interpreter, "Color", this.type), defineFunctions(environment), interpreter);
  }

  private Map<String, LoxFunction> defineFunctions(Environment environment) {
    Map<String, LoxFunction> methods = new HashMap<>();
    methods.put("red", red(environment));
    methods.put("green", green(environment));
    methods.put("blue", blue(environment));
    methods.put("black", black(environment));
    methods.put("white", white(environment));
    methods.put("set", set(environment));
    methods.put("setAlpha", setAlpha(environment));
    return methods;
  }

  

 private LoxFunction red(Environment environment) {
    return new LoxFunction(new LoxCallable() {

      @Override
      public int arity() {
        return 0;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        Color color = Color.red;
        LoxInstance instance = new LoxInstance(new LoxColor(environment, interpreter, type), interpreter);
        instance.set("r", (double)color.getRed());
        instance.set("g", (double)color.getGreen());
        instance.set("b", (double)color.getBlue());
        instance.set("a", (double)color.getAlpha());
        return instance;
      }
      
    }, environment, false, true);
  }

  private LoxFunction green(Environment environment) {
    return new LoxFunction(new LoxCallable() {

      @Override
      public int arity() {
        return 0;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        Color color = Color.green;
        LoxInstance instance = new LoxInstance(new LoxColor(environment, interpreter, type), interpreter);
        instance.set("r", (double)color.getRed());
        instance.set("g", (double)color.getGreen());
        instance.set("b", (double)color.getBlue());
        instance.set("a", (double)color.getAlpha());
        return instance;
      }

      
      
    }, environment, false, true);

    
  }

  private LoxFunction blue(Environment environment) {
    return new LoxFunction(new LoxCallable() {

      @Override
      public int arity() {
        return 0;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        Color color = Color.blue;
        LoxInstance instance = new LoxInstance(new LoxColor(environment, interpreter, type), interpreter);
        instance.set("r", (double)color.getRed());
        instance.set("g", (double)color.getGreen());
        instance.set("b", (double)color.getBlue());
        instance.set("a", (double)color.getAlpha());
        return instance;
      }
      
    }, environment, false, true);
  }

  private LoxFunction black(Environment environment) {
    return new LoxFunction(new LoxCallable() {

      @Override
      public int arity() {
        return 0;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        Color color = Color.black;
        LoxInstance instance = new LoxInstance(new LoxColor(environment, interpreter, type), interpreter);
        instance.set("r", (double)color.getRed());
        instance.set("g", (double)color.getGreen());
        instance.set("b", (double)color.getBlue());
        instance.set("a", (double)color.getAlpha());
        return instance;
      }
      
    }, environment, false, true);
  }

  private LoxFunction white(Environment environment) {
    return new LoxFunction(new LoxCallable() {

      @Override
      public int arity() {
        return 0;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        Color color = Color.white;
        LoxInstance instance = new LoxInstance(new LoxColor(environment, interpreter, type), interpreter);
        instance.set("r", (double)color.getRed());
        instance.set("g", (double)color.getGreen());
        instance.set("b", (double)color.getBlue());
        instance.set("a", (double)color.getAlpha());
        return instance;
      }
      
    }, environment, false, true);
  }

  private LoxFunction set(Environment environment) {
    return new LoxFunction(new LoxCallable() {

      @Override
      public int arity() {
        return 3;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        set("r", (double)arguments.get(0));
        set("g", (double)arguments.get(1));
        set("b", (double)arguments.get(2));
        return null;
      }
      
    }, environment, false, false);
  }

  private LoxFunction setAlpha(Environment environment) {
    return new LoxFunction(new LoxCallable() {

      @Override
      public int arity() {
        return 1;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        set("a", (double)arguments.get(0));
        return null;
      }
      
    }, environment, false, false);
  }
  

@Override
  public void defineFields() {
    
  }
}
