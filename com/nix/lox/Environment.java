package com.nix.lox;

import java.util.HashMap;
import java.util.Map;

public class Environment {
  final Environment enclosing;
  public final Map<String, Field> values = new HashMap<>();

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

  void define(String name, Object value, boolean constant, boolean stat, boolean ptr, LoxType type){
    if(new LoxType(value).type != type.type) {
      throw new RuntimeError(new Token(TokenType.IDENTIFIER, "name", value, 0), "Cant assign value of type '" + new LoxType(value).type + "' to variable '"+name+"' of type '" + type.type + "'");
    }
    put(name, new Field(value, constant, stat, ptr, type));
  }

  void define(String name, Object value, boolean constant, boolean stat, boolean ptr){
    put(name, new Field(value, constant, stat, ptr, null));
  }

  Object getAt(int distance, String name){
    Field value = ancestor(distance).values.get(name);
    return value != null ? value.value : null;
  }

  void assignAt(int distance, Token name, Object value, boolean isConstant, boolean stat, boolean ptr, LoxType type){
    ancestor(distance).put(name.lexeme, new Field(value, isConstant, stat, ptr, type));
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
      boolean ptr = values.get(name.lexeme).pointer;
      Field f = new Field(value, isConstant, stat, ptr, values.get(name.lexeme).type);
      LoxType type = values.get(name.lexeme).type;
      LoxType objType = new LoxType(value);
      if(type != null && objType != null && type.type != objType.type) {
        throw new RuntimeError(new Token(TokenType.IDENTIFIER, "name", f.value, 0), "Cant assign value of type '" + objType.type + "' to variable '"+name.lexeme+"' of type '" + type.type + "'");
      }
      put(name.lexeme, f);
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
