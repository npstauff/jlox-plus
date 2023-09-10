package com.nix.lox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Field{
  Object value;
  final boolean constant;
  final boolean isstatic;
  final boolean pointer;

  Field(Object value, boolean constant, boolean isstatic, boolean pointer){
    this.value = value;
    this.constant = constant;
    this.isstatic = isstatic;
    this.pointer = pointer;
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
  Map<String, LoxClass> templates = new HashMap<>();

  LoxClass(String name, LoxClass superclass, Map<String, LoxFunction> methods, Map<String, Field> fields,Interpreter interpreter){
    this.name = name;
    this.methods = methods;
    this.superClass = superclass;
    this.interpreter = interpreter;
    this.fields = fields;
  }

  public void setTemplates(Map<String, LoxClass> templates){
    this.templates = templates;
  }

  LoxFunction findMethod(String name, boolean staticGet){
    if(methods.containsKey(name)){
      if(staticGet){
        if(!methods.get(name).isStatic){
          throw new RuntimeError(new Token(TokenType.IDENTIFIER, "name", methods.get(name).isStatic, 0), "Cannot access non-static method '" + name + "' from static context");
        }
      }
      else{
        if(methods.get(name).isStatic){
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

  Object findField(String name, boolean staticGet){
    if(fields.containsKey(name)){
      if(staticGet){
        if(!fields.get(name).isstatic){
          throw new RuntimeError(new Token(TokenType.IDENTIFIER, "name", fields.get(name).isstatic, 0), "Cannot access non-static field '" + name + "' from static context");
        }
      }
      else{
        if(fields.get(name).isstatic){
          System.out.println("[WARNING] use field '" + name + "' from static context");
        }
      }
      return fields.get(name).value;
    }

    if(superClass != null){
      return superClass.findField(name, staticGet);
    }

    return null;
  }

  Field set(String name, Object value){
    boolean constant = false;
    boolean isstatic = false;
    boolean pointer = false;
    if(fields.containsKey(name)){
      constant = fields.get(name).constant;
      isstatic = fields.get(name).isstatic;
      pointer = fields.get(name).pointer;
      if(constant){
        throw new RuntimeError(new Token(TokenType.VAR, "name", value, 0), "Cant assign '" + value +"' to constant '" + name + "'");
      }
    } 
    else if(methods.containsKey(name)){
      constant = methods.get(name).isConstant;
      Field f = new Field(value, constant, methods.get(name).isStatic, false);
      fields.put(name, f);
      return f;
    }

    if(superClass != null){
      if(superClass.findField(name, false) != null){
        superClass.set(name, value);
      } 
      else{
        return put(name, value, constant, isstatic, pointer);
      }
    }
    
    return put(name, value, constant, isstatic, pointer);
  }

  Field put(String name, Object value, boolean constant, boolean isstatic, boolean pointer){
    Field newf = new Field(value, constant, isstatic, pointer);
    if(fields.containsKey(name)){
      Field f = fields.get(name);    
      if(f.constant){
        throw new RuntimeError(new Token(TokenType.VAR, "name", f.value, 0), "Cant assign to constant '" + name +"'");
      }
      else{
        fields.put(name, newf);
      }
    }
    else{
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
  public Object call(Interpreter interpreter, List<Object> arguments) {
    LoxInstance instance = new LoxInstance(this, interpreter);
    LoxFunction initializer = findMethod("constructor", false);
    if(initializer != null) {
      initializer.bind(instance).call(interpreter, arguments);
    }
    return instance;
  }

}
