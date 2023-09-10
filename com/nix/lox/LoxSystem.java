package com.nix.lox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nix.lox.Expr.This;
import com.nix.lox.LoxType.TypeEnum;

/**
 * LoxObject
 */
public class LoxSystem extends LoxNative{

  //public static LoxSystem system;

  LoxSystem(Environment environment, Interpreter interpreter, LoxClass type){
    super(null, null, null, null, type);
    setDetails("System", new LoxObject(environment, interpreter, "System", this.type), defineFunctions(environment), interpreter);
  }

  public Map<String, LoxFunction> defineFunctions(Environment environment) {
    Map<String, LoxFunction> methods = new HashMap<>();
    methods.put("println", debug(environment, true, false));
    methods.put("print", debug(environment, false, false));
    methods.put("cls", cls(environment));
    methods.put("errln", debug(environment, true, true));
    methods.put("err", debug(environment, false, true));
    methods.put("random", random(environment));
    methods.put("writeToFile", write(environment));
    return methods;
  }

  private LoxFunction random(Environment environment) {
    return new LoxFunction(new LoxCallable() {

      @Override
      public int arity() {
        return 3;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        double lower = (double)arguments.get(0);
        double upper = (double)arguments.get(1);
        boolean inclusive = (boolean)arguments.get(2);
        return (double)(Math.random() * (upper - lower)) + lower + (inclusive ? 1 : 0);
      }
      
    }, environment, false, true, new LoxType(name, TypeEnum.NUMBER));
  }

  LoxFunction debug(Environment environment, boolean newline, boolean err){
    return new LoxFunction(new LoxCallable() {

      @Override
      public int arity() {
        return 1;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        if(!err) System.out.print(arguments.get(0) + (newline ? "\n" : ""));
        else System.err.print(arguments.get(0) + (newline ? "\n" : ""));
        return null;
      }
      
    }, environment, false, true, new LoxType(name, TypeEnum.VOID));
  }

  LoxFunction write(Environment environment) {
    return new LoxFunction(new LoxCallable() {

      @Override
      public int arity() {
        return 3;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        String filepath = (String)arguments.get(0);
        String format = (String)arguments.get(2);
        try{
          java.io.FileWriter writer = new java.io.FileWriter(filepath);
          if(format.equals("a")) writer.append((String)arguments.get(1));
          else if(format.equals("w")) writer.write((String)arguments.get(1));
          else {writer.close(); throw new RuntimeError(new Token(TokenType.IDENTIFIER, "write", null, 1), "Invalid write format: '" + format + "'");};
          writer.close();
        }
        catch(IOException e){
          System.err.println("Error writing to file: " + filepath);
        }
        return null;
      }
      
    }, environment, false, true, new LoxType(name, TypeEnum.VOID));
  }

  LoxFunction cls(Environment environment){
    return new LoxFunction(new LoxCallable() {

      @Override
      public int arity() {
        return 0;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        System.out.print("\033[H\033[2J");  
        System.out.flush();
        return null;
      }
      
    }, environment, false, true, new LoxType(name, TypeEnum.VOID));
  }

  @Override
  public void defineFields() {
  }
}