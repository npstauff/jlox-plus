package com.nix.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Field{
  Object value;
  final Modifiers modifiers;
  final LoxType type;

  Field(Object value, Modifiers modifiers, LoxType type){
    this.value = value;
    this.modifiers = modifiers;
    this.type = type;
  }

  public Object getValue(){
    return value;
  }
}

public class LoxClass implements LoxCallable{
  Map<String, LoxFunction> methods;
  Map<String, Field> fields = new HashMap<>();
  String name;
  LoxClass superClass;
  Interpreter interpreter;
  List<Token> templates = new ArrayList<>();
  Environment environment;

  LoxClass(Environment environment, String name, LoxClass superclass, Map<String, LoxFunction> methods, Map<String, Field> fields,Interpreter interpreter){
    this.name = name;
    this.methods = methods;
    this.superClass = superclass;
    this.interpreter = interpreter;
    this.fields = fields;
    this.environment = environment;
  }

  LoxFunction findMethod(String name, boolean staticGet){
    if(methods.containsKey(name)){
      if(staticGet){
        if(!methods.get(name).modifiers.contains(TokenType.STATIC)){
          throw new RuntimeError(new Token(TokenType.IDENTIFIER, "name", methods.get(name).modifiers.contains(TokenType.STATIC), 0), "Cannot access non-static method '" + name + "' from static context");
        }
      }
      else{
        if(methods.get(name).modifiers.contains(TokenType.STATIC)){
          System.out.println("[WARNING] use method '" + name + "' from static context");
        }
      }
      return methods.get(name);
    }

    if(superClass != null){
      return superClass.findMethod(name, staticGet);
    }

    return null;
  }

  Field findField(String name, boolean staticGet){
    if(fields.containsKey(name)){
      if(staticGet){
        if(!fields.get(name).modifiers.contains(TokenType.STATIC)){
          throw new RuntimeError(new Token(TokenType.IDENTIFIER, "name", fields.get(name).modifiers.contains(TokenType.STATIC), 0), "Cannot access non-static field '" + name + "' from static context");
        }
      }
      else{
        if(fields.get(name).modifiers.contains(TokenType.STATIC)){
          System.out.println("[WARNING] use field '" + name + "' from static context");
        }
      }
      if(fields.get(name).value instanceof LoxProperty) {
        LoxProperty property = (LoxProperty) fields.get(name).value;
        property.value = getProperty(property);
        return new Field(property.value, fields.get(name).modifiers, fields.get(name).type);
      }
      return fields.get(name);
    }

    if(superClass != null){
      return superClass.findField(name, staticGet);
    }

    return null;
  }

  public Object getProperty(LoxProperty property) {
      return property.get(interpreter);
  }

  Field set(String name, Object value){
    Modifiers modifiers = new Modifiers();
    LoxType type = new LoxType(value);
    if(fields.containsKey(name)){
      modifiers = fields.get(name).modifiers;
      if(modifiers.contains(TokenType.CONST)){
        throw new RuntimeError(new Token(TokenType.VAR, "name", value, 0), "Cant assign '" + value +"' to constant '" + name + "'");
      }
    } 
    else if(methods.containsKey(name)){
      modifiers = methods.get(name).modifiers;
      Field f = new Field(value, modifiers, methods.get(name).returnType);
      fields.put(name, f);
      return f;
    }

    if(superClass != null){
      if(superClass.findField(name, false) != null){
        superClass.set(name, value);
      } 
      else{
        return put(name, value, modifiers, type);
      }
    }
    
    return put(name, value, modifiers, type);
  }

  public static void checkType(LoxType l, LoxType r){
    if(!l.matches(r)){
      throw new RuntimeError(new Token(TokenType.IDENTIFIER, "name", l, 0), "Type mismatch: '" + l + "' is not a '" + r + "'");
    }
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

  Field put(String name, Object value, Modifiers modifiers, LoxType type){
    Interpreter.checkModifiers(modifiers, type, value, name);
    Field newf = new Field(value, modifiers, type);
    if(fields.containsKey(name)){
      Field f = fields.get(name);
      if(f.value instanceof LoxProperty) {
        setProperty(name, f, value);
        return f;
      }
      checkType(new LoxType(f.value), type);    
      if(f.modifiers.contains(TokenType.CONST)){
        throw new RuntimeError(new Token(TokenType.VAR, "name", f.value, 0), "Cant assign to constant '" + name +"'");
      }
      else{
        fields.put(name, newf);
      }
    }
    else{
      if(newf.value instanceof LoxProperty) {
        setProperty(name, newf, value);
        return newf;
      }
      fields.put(name, newf);
    }
    return newf;
  }

  @Override
  public String toString(){
    return name;
  }

  @Override
  public int arity() {
    LoxFunction initializer = findMethod("constructor", false);
    if(initializer==null) return 0;
    return initializer.arity();
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> arguments, List<LoxClass> templates) {
    LoxInstance instance = new LoxInstance(this, interpreter);
    bindTemplates(templates, instance);
    LoxFunction initializer = findMethod("constructor", false);
    if(initializer != null) {
      initializer.bind(instance, interpreter).call(interpreter, arguments, templates);
    }
    return instance;
  }

  public void bindTemplates(List<LoxClass> generics, LoxInstance instance) {
    if(templates.size() == 0) return;
    if(generics.size() != templates.size()) {
      throw new RuntimeError(new Token(TokenType.IDENTIFIER, "name", generics, 0), "Incorrect number of generic types for class '" + name + "'");
    }
    for(int i = 0; i < generics.size(); i++) {
      LoxClass generic = generics.get(i);
      Token template = templates.get(i);
      environment.assign(template, generic);
    }
  }

}
