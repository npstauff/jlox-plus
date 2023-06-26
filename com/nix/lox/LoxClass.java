package com.nix.lox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Field{
  Object value;
  final boolean constant;

  Field(Object value, boolean constant){
    this.value = value;
    this.constant = constant;
  }
}

public class LoxClass implements LoxCallable{
  Map<String, LoxFunction> methods;
  Map<String, Field> fields = new HashMap<>();
  String name;
  LoxClass superClass;
  Interpreter interpreter;

  LoxClass(String name, LoxClass superclass, Map<String, LoxFunction> methods, Map<String, Field> fields,Interpreter interpreter){
    this.name = name;
    this.methods = methods;
    this.superClass = superclass;
    this.interpreter = interpreter;
    this.fields = fields;
  }

  LoxFunction findMethod(String name){
    if(methods.containsKey(name)){
      return methods.get(name);
    }

    if(superClass != null){
      return superClass.findMethod(name);
    }

    return null;
  }

  Object findField(String name){
    if(fields.containsKey(name)){
      return fields.get(name).value;
    }

    if(superClass != null){
      return superClass.findField(name);
    }

    return null;
  }

  void set(String name, Object value){
    boolean constant = false;
    if(fields.containsKey(name)){
      constant = fields.get(name).constant;
      if(constant){
        throw new RuntimeError(new Token(TokenType.VAR, "name", value, 0), "Cant assign '" + value +"' to constant '" + name + "'");
      }
    } 
    else if(methods.containsKey(name)){
      constant = methods.get(name).isConstant;
      fields.put(name, new Field(value, constant));
    }

    if(superClass != null){
      if(superClass.findField(name) != null){
        superClass.set(name, value);
      } 
      else{
        put(name, value, constant);
      }
    }
    else{
      put(name, value, constant);
    }
  }

  void put(String name, Object value, boolean constant){
    if(fields.containsKey(name)){
      Field f = fields.get(name);
      if(f.constant){
        throw new RuntimeError(new Token(TokenType.VAR, "name", f.value, 0), "Cant assign to constant '" + name +"'");
      }
      else{
        fields.put(name, new Field(value, constant));
      }
    }
    else{
      fields.put(name, new Field(value, constant));
    }
  }

  @Override
  public String toString(){
    return name;
  }

  @Override
  public int arity() {
    LoxFunction initializer = findMethod("init");
    if(initializer==null) return 0;
    return initializer.arity();
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> arguments) {
    LoxInstance instance = new LoxInstance(this, interpreter);
    LoxFunction initializer = findMethod("init");
    if(initializer != null) {
      initializer.bind(instance).call(interpreter, arguments);
    }
    return instance;
  }
}
