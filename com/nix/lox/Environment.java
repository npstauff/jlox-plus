package com.nix.lox;

import java.util.HashMap;
import java.util.Map;

public class Environment {
  final Environment enclosing;
  final Map<String, Field> values = new HashMap<>();

  Environment() {
    enclosing = null;
  }

  Environment(Environment enclosing){
    this.enclosing = enclosing;
  }

  Object get(Token name){
    if(values.containsKey(name.lexeme)){
      if(values.get(name.lexeme).isstatic){
        System.out.println("[WARNING] Accessing static context '" + name.lexeme + "' in global scope in not recommended");
      }
      return values.get(name.lexeme).value;
    }

    if(enclosing != null) return enclosing.get(name);

    throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
  }

  void define(String name, Object value, boolean constant, boolean stat){
    put(name, new Field(value, constant, stat));
  }

  Object getAt(int distance, String name){
    Field value = ancestor(distance).values.get(name);
    return value != null ? value.value : null;
  }

  void assignAt(int distance, Token name, Object value, boolean isConstant, boolean stat){
    ancestor(distance).put(name.lexeme, new Field(value, isConstant, stat));
  }

  Environment ancestor(int distance){
    Environment environment = this;
    for (int i = 0; i < distance; i++) {
      environment = environment.enclosing;
    }

    return environment;
  }

  void put(String name, Field field){
    if(values.containsKey(name)){
      Field f = values.get(name);
      if(f.constant){
        throw new RuntimeError(new Token(TokenType.IDENTIFIER, "name", f.value, 0), "Cant assign to constant value '" + name +"'");
      }
      else{
        values.put(name, field);
      }
    }
    else{
      values.put(name, field);
    }
  }

  void assign(Token name, Object value) {
    boolean isConstant = false;
    if (values.containsKey(name.lexeme)) {
      isConstant = values.get(name.lexeme).constant;
      boolean stat = values.get(name.lexeme).isstatic;
      put(name.lexeme, new Field(value, isConstant, stat));
      return;
    }

    if(enclosing != null) {
      enclosing.assign(name, value);
      return;
    }

    throw new RuntimeError(name,
        "Undefined variable '" + name.lexeme + "'.");
  }
}
