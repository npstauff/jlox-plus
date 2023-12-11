package com.nix.lox;

import java.util.HashMap;
import java.util.Map;

public class Environment {
  final Environment enclosing;
  public final Map<String, Field> values = new HashMap<>();
  public Interpreter interpreter;

  Environment(Interpreter interpreter) {
    enclosing = null;
    this.interpreter = interpreter;
  }

  Environment(Environment enclosing, Interpreter interpreter){
    this.enclosing = enclosing;
    this.interpreter = interpreter;
  }

  Object get(Token name){
    if(values.containsKey(name.lexeme)){
      if(values.get(name.lexeme).modifiers.contains(TokenType.STATIC)){
        System.out.println("[WARNING] Accessing static context '" + name.lexeme + "' in global scope in not recommended");
      }
      if(values.get(name.lexeme).value instanceof LoxProperty) {
        LoxProperty property = (LoxProperty) values.get(name.lexeme).value;
        return property.get(interpreter);
      }
      return values.get(name.lexeme).value;
    }

    if(enclosing != null) return enclosing.get(name);

    throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
  }

  Field getField(Token name){
    if(values.containsKey(name.lexeme)){
      if(values.get(name.lexeme).modifiers.contains(TokenType.STATIC)){
        System.out.println("[WARNING] Accessing static context '" + name.lexeme + "' in global scope in not recommended");
      }
      return values.get(name.lexeme);
    }

    if(enclosing != null) return enclosing.getField(name);

    throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
  }

  void define(String name, Object value, Modifiers modifiers, LoxType type){
    LoxType valueType = new LoxType(value);
    if(value != null && valueType.mismatch(type)) {
      throw new RuntimeError(new Token(TokenType.IDENTIFIER, "name", value, 0), "Cant assign value of type '" + valueType + "' to variable '"+name+"' of type '" + type + "'");
    }
    put(name, new Field(value, modifiers, type));
  }

  void define(String name, Object value, Modifiers modifiers){
    put(name, new Field(value, modifiers, null));
  }

  Object getAt(int distance, String name){
    Environment environment = ancestor(distance);
    Field value = environment.values.get(name);
    return value != null ? value.value : null;
  }

  Field getFieldAt(int distance, String name){
    Field value = ancestor(distance).values.get(name);
    return value != null ? value : null;
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
    Interpreter.checkModifiers(field, name);
    if(values.containsKey(name)){
      Field f = values.get(name);

      if(f.modifiers != null && f.modifiers.contains(TokenType.CONST)){
        throw new RuntimeError(new Token(TokenType.IDENTIFIER, "name", f.value, 0), "Cant assign to constant value '" + name +"'");
      }
      values.put(name, field);
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
      if(type != null && objType != null && type.mismatch(objType)) {
        throw new RuntimeError(new Token(TokenType.IDENTIFIER, "name", f.value, 0), "Cant assign value of type '" + objType + "' to variable '"+name.lexeme+"' of type '" + type + "'");
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
