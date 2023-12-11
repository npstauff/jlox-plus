package com.nix.lox;

import java.util.HashMap;
import java.util.Map;

public abstract class LoxNative extends LoxClass {
   LoxClass type;

  LoxNative(String name, LoxClass supero, Map<String, LoxFunction> methods, Interpreter interpreter, LoxClass type){
    super(null, null, null, null, new HashMap<String, Field>(), null);
    this.type = type == null ? this : type;
    defineFields();
  }

  void setDetails(String name, LoxClass supero, Map<String, LoxFunction> methods, Interpreter interpreter){
    this.name = name;
    this.superClass = supero;
    this.methods = methods;
    this.interpreter = interpreter;
  }

  public abstract void defineFields();
}
