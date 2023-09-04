package com.nix.lox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Field{
  Object value;
  final boolean constant;
  final boolean isstatic;

  Field(Object value, boolean constant, boolean isstatic){
    this.value = value;
    this.constant = constant;
    this.isstatic = isstatic;
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
      return fields.get(name).value;
    }

    if(superClass != null){
      return superClass.findField(name, staticGet);
    }

    return null;
  }

  void set(String name, Object value){
    boolean constant = false;
    boolean isstatic = false;
    if(fields.containsKey(name)){
      constant = fields.get(name).constant;
      isstatic = fields.get(name).isstatic;
      if(constant){
        throw new RuntimeError(new Token(TokenType.VAR, "name", value, 0), "Cant assign '" + value +"' to constant '" + name + "'");
      }
    } 
    else if(methods.containsKey(name)){
      constant = methods.get(name).isConstant;
      fields.put(name, new Field(value, constant, methods.get(name).isStatic));
    }

    if(superClass != null){
      if(superClass.findField(name, false) != null){
        superClass.set(name, value);
      } 
      else{
        put(name, value, constant, isstatic);
      }
    }
    else{
      put(name, value, constant, isstatic);
    }
  }

  void put(String name, Object value, boolean constant, boolean isstatic){
    if(fields.containsKey(name)){
      Field f = fields.get(name);
      if(f.constant){
        throw new RuntimeError(new Token(TokenType.VAR, "name", f.value, 0), "Cant assign to constant '" + name +"'");
      }
      else{
        fields.put(name, new Field(value, constant, isstatic));
      }
    }
    else{
      fields.put(name, new Field(value, constant, isstatic));
    }
  }

  @Override
  public String toString(){
    return name;
  }

  @Override
  public int arity() {
    LoxFunction initializer = findMethod("init", false);
    if(initializer==null) return 0;
    return initializer.arity();
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> arguments) {
    LoxInstance instance = new LoxInstance(this, interpreter);
    LoxFunction initializer = findMethod("init", false);
    if(initializer != null) {
      initializer.bind(instance).call(interpreter, arguments);
    }
    return instance;
  }
}
