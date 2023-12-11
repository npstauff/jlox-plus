package com.nix.lox;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nix.lox.LoxType.TypeEnum;

/**
 * LoxObject
 */
public class LoxSystem extends LoxNative{

  //public static LoxSystem system;
  final Environment environment;
  Socket clientSocket = null;
  InputStream input = null;
  OutputStream output = null;

  LoxSystem(Environment environment, Interpreter interpreter, LoxClass type){
    super(null, null, null, null, type);
    setDetails("System", new LoxObject(environment, interpreter, "System", this.type), defineFunctions(environment), interpreter);
    this.environment = environment;
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
    methods.put("readFile", readFile());
    methods.put("deleteFile", deleteFile());
    methods.put("createFile", createFile());
    methods.put("fileExists", fileExists());
    methods.put("listFiles", getFiles());
    methods.put("connect", connect());
    methods.put("read", read());
    methods.put("write", writeToSocket());
    methods.put("close", close());
    return methods;
  }

  private LoxFunction readFile() {
    return new LoxFunction(new LoxCallable() {
        @Override
        public int arity() {
          return 1;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments, List<LoxClass> templates) {
          String path = (String)arguments.get(0);
          try {
            File f = new File(path);
            if(!f.exists()) {
              throw new Exception();
            }
            java.io.FileReader reader = new java.io.FileReader(f);

            String contents = "";
            int i;
            while((i = reader.read()) != -1) {
              contents += (char)i;
            }
            reader.close();
            return contents;
          }
          catch (Exception e) {
            throw new RuntimeError(Token.basic(), "Error fetching file at specified path '" + path + "'");
          }
        }
    }, environment, false, new LoxType("string", TypeEnum.STRING), new Modifiers(TokenType.STATIC));
  }

  private LoxFunction getFiles() {
    return new LoxFunction(new LoxCallable() {
        @Override
        public int arity() {
          return 1;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments, List<LoxClass> templates) {
          String path = (String)arguments.get(0);
          try {
            File f = new File(path);
            if(!f.exists()) {
              throw new Exception();
            }
            File[] contents = f.listFiles();

            LoxArray arr = new LoxArray(new LoxType("string[]", TypeEnum.STRING), contents.length);
            for(int i = 0; i < contents.length; i++) {
              arr.set(i, contents[i].getPath());
            }

            return arr;
          }
          catch (Exception e) {
            throw new RuntimeError(Token.basic(), "Error fetching files at specified path '" + path + "'");
          }
        }
    }, environment, false, new LoxType("string", TypeEnum.STRING), new Modifiers(TokenType.STATIC));
  }

  private LoxFunction deleteFile() {
    return new LoxFunction(new LoxCallable() {
        @Override
        public int arity() {
          return 1;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments, List<LoxClass> templates) {
          String path = (String)arguments.get(0);
          try {
            File f = new File(path);
            if(!f.exists()) {
              throw new Exception();
            }
            f.delete();
            return null;
          }
          catch (Exception e) {
            throw new RuntimeError(Token.basic(), "Error deleting file at specified path '" + path + "'");
          }
        }
    }, environment, false, new LoxType("string", TypeEnum.STRING), new Modifiers(TokenType.STATIC));
  }

  private LoxFunction createFile() {
    return new LoxFunction(new LoxCallable() {
        @Override
        public int arity() {
          return 1;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments, List<LoxClass> templates) {
          String path = (String)arguments.get(0);
          try {
            File f = new File(path);
            if(!f.exists()) {
              //create new file
              f.createNewFile();
            }
            else {
              throw new Exception();
            }
            return null;
          }
          catch (Exception e) {
            throw new RuntimeError(Token.basic(), "Can't create new file, file already exists at specified path '" + path + "'");
          }
        }
    }, environment, false, new LoxType("string", TypeEnum.STRING), new Modifiers(TokenType.STATIC));
  }

  private LoxFunction fileExists() {
    return new LoxFunction(new LoxCallable() {
        @Override
        public int arity() {
          return 1;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments, List<LoxClass> templates) {
          String path = (String)arguments.get(0);
          try {
            File f = new File(path);
            return f.exists();
          }
          catch (Exception e) {
            throw new RuntimeError(Token.basic(), "Error fetching file at specified path '" + path + "'");
          }
        }
    }, environment, false, new LoxType("string", TypeEnum.STRING), new Modifiers(TokenType.STATIC));
  }

  private LoxFunction connect() {
    return new LoxFunction(new LoxCallable() {

      @Override
      public int arity() {
        return 1;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments, List<LoxClass> templates) {
        int port = (int)((double)arguments.get(0));
        try {
          ServerSocket serverSocket = new ServerSocket(port);
          clientSocket = serverSocket.accept();

          input = clientSocket.getInputStream();
          output = clientSocket.getOutputStream();

        } catch (Exception e) {
          Lox.error(0, "Connection to port " + port + " failed");
          try{
            closeNetwork();
          }
          catch(IOException e2){
            Lox.error(0, "Error closing connection");
          }
        }
        return null;
      }
      
    }, environment, false, new LoxType("void", TypeEnum.VOID), new Modifiers(TokenType.STATIC));
  }

  private LoxFunction read() {
    return new LoxFunction(new LoxCallable() {

      @Override
      public int arity() {
        return 1;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments, List<LoxClass> templates) {
        if(input == null) {
          Lox.error(0, "No connection to read from, input was either closed or never opened");
          return null;
        }
        int bytesize = (int)((double)arguments.get(0));
        byte[] bytes = new byte[bytesize];
        try {
          int bytesRead = input.read(bytes);
          return new String(bytes, 0, bytesRead);
        } catch (Exception e) {
          Lox.error(0, "Error reading from input stream");
          try{
            closeNetwork();
          }
          catch(IOException e2){
            Lox.error(0, "Error closing connection");
          }
        }
        return null;
      }
      
    }, environment, false, new LoxType("string", TypeEnum.STRING), new Modifiers(TokenType.STATIC));
  }

  private LoxFunction writeToSocket() {
    return new LoxFunction(new LoxCallable() {

      @Override
      public int arity() {
        return 1;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments, List<LoxClass> templates) {
        if(output == null) {
          Lox.error(0, "No connection to write to, output was either closed or never opened");
          return null;
        }
        String message = (String)arguments.get(0);
        try {
          output.write(message.getBytes());
        } catch (IOException e) {
          Lox.error(0, "Error writing to output stream");
          try{
            closeNetwork();
          }
          catch(IOException e2){
            Lox.error(0, "Error closing connection");
          }
        }
        return null;
      }
      
    }, environment, false, new LoxType("void", TypeEnum.VOID), new Modifiers(TokenType.STATIC));
  }

  private LoxFunction close() {
    return new LoxFunction(new LoxCallable() {

      @Override
      public int arity() {
        return 0;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments, List<LoxClass> templates) {
        try {
          closeNetwork();
        } catch (IOException e) {
          Lox.error(0, "Error closing connection");
        }
        return null;
      }
      
    }, environment, false, new LoxType("void", TypeEnum.VOID), new Modifiers(TokenType.STATIC));
  }

  public void closeNetwork() throws IOException {
      if(input != null) input.close();
      if(output != null) output.close();
      if(clientSocket != null) clientSocket.close();
  } 

  private LoxFunction random(Environment environment) {
    return new LoxFunction(new LoxCallable() {

      @Override
      public int arity() {
        return 3;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments, List<LoxClass> templates) {
        double lower = (double)arguments.get(0);
        double upper = (double)arguments.get(1);
        boolean inclusive = (boolean)arguments.get(2);
        return (double)(Math.random() * (upper - lower)) + lower + (inclusive ? 1 : 0);
      }
      
    }, environment, false, new LoxType(name, TypeEnum.NUMBER), new Modifiers(TokenType.STATIC));
  }

  LoxFunction debug(Environment environment, boolean newline, boolean err){
    return new LoxFunction(new LoxCallable() {

      @Override
      public int arity() {
        return 1;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments, List<LoxClass> templates) {
        if(!err) System.out.print(arguments.get(0) + (newline ? "\n" : ""));
        else System.err.print(arguments.get(0) + (newline ? "\n" : ""));
        return null;
      }
      
    }, environment, false, new LoxType(name, TypeEnum.VOID), new Modifiers(TokenType.STATIC));
  }

  LoxFunction write(Environment environment) {
    return new LoxFunction(new LoxCallable() {

      @Override
      public int arity() {
        return 2;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments, List<LoxClass> templates) {
        String filepath = (String)arguments.get(0);
        try{
          java.io.FileWriter writer = new java.io.FileWriter(filepath);
          writer.write((String)arguments.get(1));
          writer.close();
        }
        catch(IOException e){
          System.err.println("Error writing to file: " + filepath);
        }
        return null;
      }
      
    }, environment, false, new LoxType(name, TypeEnum.VOID), new Modifiers(TokenType.STATIC));
  }

  LoxFunction cls(Environment environment){
    return new LoxFunction(new LoxCallable() {

      @Override
      public int arity() {
        return 0;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments, List<LoxClass> templates) {
        System.out.print("\033[H\033[2J");  
        System.out.flush();
        return null;
      }
      
    }, environment, false, new LoxType(name, TypeEnum.VOID), new Modifiers(TokenType.STATIC));
  }

  @Override
  public void defineFields() {
    put("network", this, new Modifiers(TokenType.STATIC), new LoxType(this));
  }
}