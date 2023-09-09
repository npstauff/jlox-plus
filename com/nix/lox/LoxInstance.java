package com.nix.lox;

import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

public class LoxInstance {
  LoxClass klass;
  //private final Map<String, Object> fields = new HashMap<>();
  private final Interpreter interpreter;

  LoxInstance(LoxClass klass, Interpreter interpreter) {
    this.klass = klass;
    this.interpreter = interpreter;
  }

  Object get(Token name, boolean staticGet){
    if(klass.findField(name.lexeme, staticGet) != null){
      return klass.findField(name.lexeme, staticGet);
    }

    LoxFunction method = klass.findMethod(name.lexeme, staticGet);
    if(method != null) return method.bind(this);

    throw new RuntimeError(name, "Undefined property " + name.lexeme);
  }

  void set(Token name, Object value){
    //fields.put(name.lexeme, value);
    klass.set(name.lexeme, value);
  }

  
  void set(String name, Object value){
    //fields.put(name.lexeme, value);
    klass.set(name, value);
  }

  @Override
  public String toString() {
    return (String)klass.findMethod("toString", false).call(interpreter, new ArrayList<>());
  }

  
  protected void finalize() {
    LoxFunction destructor = klass.findMethod("destructor", false);
    if(destructor != null) {
      destructor.bind(this).call(interpreter, null);
    }
  }
}
