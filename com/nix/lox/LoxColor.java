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

@Override
  public void defineFields() {
    
  }
}
