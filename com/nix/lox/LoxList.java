package com.nix.lox;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.nix.lox.LoxType.TypeEnum;

public class LoxList extends LoxNative{
  private List<Object> items = new ArrayList<Object>();


  LoxList(Environment environment, Interpreter interpreter, LoxClass type){
    super(null, null, null, null, type);
    setDetails("List", new LoxObject(environment, interpreter, "List", this.type), defineFunctions(environment), interpreter);
  }

  private Map<String, LoxFunction> defineFunctions(Environment environment) {
    Map<String, LoxFunction> methods = new HashMap<>();
    methods.put("add", add(environment));
    methods.put("remove", remove(environment));
    methods.put("indexOf", indexOf(environment));
    methods.put("get", get(environment));
    methods.put("length", length(environment));
    methods.put("first", first(environment));
    methods.put("last", last(environment));
    methods.put("contains", contains(environment));
    return methods;
  }

 private LoxFunction contains(Environment environment) {
    return new LoxFunction(new LoxCallable() {

      @Override
      public int arity() {
        return 1;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        for(Object item : items){
          if(item.equals(arguments.get(0))){
            return true;
          }
        }
        return false;
      }
      
    }, environment, false, new LoxType(name, TypeEnum.BOOLEAN), new Modifiers(TokenType.STATIC));
  }

private LoxFunction get(Environment environment) {
    return new LoxFunction(new LoxCallable() {

      @Override
      public int arity() {
        return 1;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        return items.get((int)((double)arguments.get(0)));
      }
      
    }, environment, false, new LoxType(name, TypeEnum.OBJECT), new Modifiers(TokenType.STATIC));
  }

private LoxFunction add(Environment environment) {
    return new LoxFunction(new LoxCallable() {

      @Override
      public int arity() {
        return 1;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        items.add(arguments.get(0));
        return null;
      }
      
    }, environment, false, new LoxType(name, TypeEnum.VOID), new Modifiers(TokenType.STATIC));
  }

  private LoxFunction remove(Environment environment) {
    return new LoxFunction(new LoxCallable() {

      @Override
      public int arity() {
        return 1;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        items.remove((int)((double)arguments.get(0)));
        return null;
      }
      
    }, environment, false, new LoxType(name, TypeEnum.VOID), new Modifiers(TokenType.STATIC));
  }

  private LoxFunction indexOf(Environment environment) {
    return new LoxFunction(new LoxCallable() {

      @Override
      public int arity() {
        return 1;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        return items.indexOf((arguments.get(0)));
      }
      
    }, environment, false, new LoxType(name, TypeEnum.NUMBER), new Modifiers(TokenType.STATIC));
  }

  private LoxFunction length(Environment environment) {
    return new LoxFunction(new LoxCallable() {

      @Override
      public int arity() {
        return 0;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        return (double)items.size();
      }
      
    }, environment, false, new LoxType(name, TypeEnum.NUMBER), new Modifiers(TokenType.STATIC));
  }

  private LoxFunction first(Environment environment) {
    return new LoxFunction(new LoxCallable() {

      @Override
      public int arity() {
        return 0;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        return items.get(0);
      }
      
    }, environment, false, new LoxType(name, TypeEnum.OBJECT), new Modifiers(TokenType.STATIC));
  }

  private LoxFunction last(Environment environment) {
    return new LoxFunction(new LoxCallable() {

      @Override
      public int arity() {
        return 0;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        return items.get(items.size() - 1);
      }
      
    }, environment, false, new LoxType(name, TypeEnum.OBJECT), new Modifiers(TokenType.STATIC));
  }

 @Override
  public void defineFields() {
    
  }
}
