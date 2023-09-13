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
      if(values.get(name.lexeme).modifiers.contains(TokenType.STATIC)){
        System.out.println("[WARNING] Accessing static context '" + name.lexeme + "' in global scope in not recommended");
      }
      return values.get(name.lexeme).value;
    }

    if(enclosing != null) return enclosing.get(name);

    throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
  }

  void define(String name, Object value, Modifiers modifiers, LoxType type){
    if(value != null && new LoxType(value).mismatch(type)) {
      throw new RuntimeError(new Token(TokenType.IDENTIFIER, "name", value, 0), "Cant assign value of type '" + new LoxType(value).name + "' to variable '"+name+"' of type '" + type.name + "'");
    }
    put(name, new Field(value, modifiers, type));
  }

  void define(String name, Object value, Modifiers modifiers){
    put(name, new Field(value, modifiers, null));
  }

  Object getAt(int distance, String name){
    Field value = ancestor(distance).values.get(name);
    return value != null ? value.value : null;
  }

  void assignAt(int distance, Token name, Object value, Modifiers modifiers, LoxType type){
    ancestor(distance).put(name.lexeme, new Field(value, modifiers, type));
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

      if(f.modifiers.contains(TokenType.CONST)){
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
    Modifiers modifiers = new Modifiers();
    if (values.containsKey(name.lexeme)) {
      modifiers = values.get(name.lexeme).modifiers;
      Field f = new Field(value, modifiers, values.get(name.lexeme).type);
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
