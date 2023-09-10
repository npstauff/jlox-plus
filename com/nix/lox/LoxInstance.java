package com.nix.lox;

import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

public class LoxInstance {
  LoxClass klass;
  private final Map<String, Object> fields = new HashMap<>();
  private final Interpreter interpreter;

  LoxInstance(LoxClass klass, Interpreter interpreter) {
    this.klass = klass;
    this.interpreter = interpreter;

    for(Map.Entry<String, Field> field : klass.fields.entrySet()){
      if(!field.getValue().isstatic) fields.put(field.getKey(), field.getValue().value);
    }
  }

  Object get(Token name, boolean staticGet){
    return get(name.lexeme, staticGet, "Undefined property '" + name.lexeme + "'");
  }

  Object get(String name, boolean staticGet){
    return get(name, staticGet, "Undefined property '" + name + "'");
  }

  Object get(String name, boolean staticGet, String message){
    if(staticGet) {
      if(klass.findField(name, staticGet) != null){
        return klass.findField(name, staticGet);
      }
    }
    else{
      if(fields.containsKey(name)){
        return fields.get(name);
      }
    }

    LoxFunction method = klass.findMethod(name, staticGet);
    if(method != null) return method.bind(this);

    throw new RuntimeError(new Token(TokenType.NIL, name, method, 0), message);
  }

  void set(Token name, Object value){
    set(name.lexeme, value);
  }

  
  void set(String name, Object value){
    //fields.put(name, value);
    fields.put(name, klass.set(name, value).value);
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
