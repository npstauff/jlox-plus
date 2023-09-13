package com.nix.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nix.lox.LoxType.TypeEnum;

public class LoxObject extends LoxNative{

  LoxObject(Environment environment, Interpreter interpreter, String name, LoxClass type) {
    super(null, null, null, null, type);
    setDetails(name, null, defineFunctions(environment), interpreter);
  }
  
  private Map<String, LoxFunction> defineFunctions(Environment environment){
    Map<String, LoxFunction> methods = new HashMap<>();
    methods.put("init", init(environment));
    methods.put("toString", toString(environment));
    //methods.put("typeof", typeof(environment));
    methods.put("fields", fields(environment));
    return methods;
  }

  private LoxFunction fields(Environment environment) {
    return new LoxFunction(new LoxCallable() {

      @Override
      public int arity() {
        return 0;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        LoxInstance map = new LoxInstance(new LoxMap(environment, interpreter, type), interpreter);
        for(String s : type.fields.keySet()){
          ArrayList<Object> args = new ArrayList<>();
          args.add(s);
          args.add(type.fields.get(s).value);
          map.klass.findMethod("put", false).call(interpreter, args);
        }
        return map;
      }
      
    }, environment, false, new LoxType(name, TypeEnum.OBJECT), new Modifiers(TokenType.STATIC));
  }

  public void defineFields(){
    put("name", "Object$"+this.type, Modifiers.empty(), new LoxType("name", TypeEnum.STRING));
  }

  private LoxFunction init(Environment environment){
    return new LoxFunction(new LoxCallable() {

        @Override
        public int arity() {
          return 0;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return null;
        }
        
      }, environment, true, new LoxType(name, TypeEnum.VOID), new Modifiers(TokenType.STATIC));
  }

  private LoxFunction toString(Environment environment) {
      return new LoxFunction(new LoxCallable() {

        @Override
        public int arity() {
          return 0;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            if(type.methods.containsKey("toString") && !type.name.equals("Object")){
              return type.findMethod("toString", false).call(interpreter, new ArrayList<>());
            }
            else{
              return defaultToString();
            }
        }

        public String defaultToString(){
          return "Lox.Type$"+type;
        }
        
      }, environment, false, new LoxType(name, TypeEnum.STRING), new Modifiers(TokenType.STATIC));
  }

}
