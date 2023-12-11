package com.nix.lox;

import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LoxInstance implements LoxCallable{
  LoxClass klass;
  private final Map<String, Field> fields = new HashMap<>();
  private final Interpreter interpreter;

  LoxInstance(LoxClass klass, Interpreter interpreter) {
    this.klass = klass;
    this.interpreter = interpreter;

    for(Map.Entry<String, Field> field : klass.fields.entrySet()){
      if(!field.getValue().modifiers.contains(TokenType.STATIC)) fields.put(field.getKey(), field.getValue());
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
        if(fields.get(name).value instanceof LoxProperty) {
          LoxProperty property = (LoxProperty) fields.get(name).value;
          return getProperty(property);
        }
        return fields.get(name).value;
      }
    }

    LoxFunction method = klass.findMethod(name, staticGet);
    if(method != null) return method.bind(this, interpreter);

    throw new RuntimeError(new Token(TokenType.NIL, name, method, 0), message);
  }

  Field getField(String name, boolean staticGet, String message){
    if(staticGet) {
      if(klass.findField(name, staticGet) != null){
        return klass.findField(name, staticGet);
      }
    }
    else{
      if(fields.containsKey(name)){
        if(fields.get(name).value instanceof LoxProperty) {
          LoxProperty property = (LoxProperty) fields.get(name).value;
          property.value = getProperty(property);
          return new Field(property, fields.get(name).modifiers, fields.get(name).type);
        }
        return fields.get(name);
      }
    }
    throw new RuntimeError(new Token(TokenType.NIL, name, null, 0), message);
  }

  void set(Token name, Object value){
    set(name.lexeme, value);
  }

  
  void set(String name, Object value){
    //fields.put(name, value);
    Field obj = getField(name, false, "Field not found '" + name + "'");
    setProperty(name, obj, value);
    Interpreter.checkModifiers(obj.modifiers, obj.type, value, name);
    fields.put(name, klass.set(name, value));
  }

  public void setProperty(String name, Field obj, Object value) {
    if(obj.value instanceof LoxProperty) {
      LoxProperty property = (LoxProperty) obj.value;
      ArrayList<Object> args = new ArrayList<>();
      args.add(value);
      if(property.getSet() != null) property.value = property.set(interpreter, args);
      else throw new RuntimeError(property.getName(), "Cannot assign to property '" + property.getName().lexeme + "' because it is read-only");
      obj.value = property;
      fields.put(name, obj);
      return;
    }
  }

  public Object getProperty(LoxProperty property) {
      return property.get(interpreter);
  }

  @Override
  public String toString() {
    return (String)klass.findMethod("toString", false).call(interpreter, new ArrayList<>(), new ArrayList<>());
  }

  @Override
  public int arity() {
    return klass.arity();
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> arguments, List<LoxClass> templates) {
    return klass.call(interpreter, arguments, templates);
  }
}
