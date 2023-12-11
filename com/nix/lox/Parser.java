package com.nix.lox;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.nix.lox.LoxType.TypeEnum;
import com.nix.lox.Stmt.Property;

import static com.nix.lox.TokenType.*;

public class Parser {
    private static class ParseError extends RuntimeException {}

    private static final TokenType[] types = {
      NUMPARAM,
      STRPARAM,
      BOOLEAN,
      VOID,
      OBJPARAM,
      ANY,
      BYTE,
      TYPE
    };


    private List<String> imports = new ArrayList<>();
    private boolean definedModule = false;
    private List<Token> tokens = new ArrayList<>();
    private int current = 0;

    Parser(List<Token> tokens, String source) {
      this.tokens = tokens;
    }

    List<Stmt> parse(){
      List<Stmt> statements = new ArrayList<>();
      while(!isAtEnd()){
        statements.add(declaration());
      } 

      return statements;
    }

    private Stmt declaration(){
      try{
        if(match(CLASS)) return classDeclaration();
        if(match(INTERFACE)) return interfaceDeclaration();
        if(match(ENUM)) return enumDeclaration();
        Modifiers modifiers = new Modifiers();
        while(matchModifier()){
          modifiers.add(previous().type);
        }
        if(match(FUN)) return function("function", modifiers);
        LoxType type = null;
        if((type = matchType(modifiers)) != null) return varDeclaration(modifiers, type);

        return statement();
      } catch(ParseError e){
        synchronize();
        return null;
      }
    }

    private void expandType(LoxType type, boolean findBracket) {
      if(type.type == TypeEnum.OBJECT) {
        if(match(FUN)) {
          type.name = getFuncPtr();
        }
        else{
          type.name = consume(IDENTIFIER, "Expect type name after obj").lexeme;
        }
      }
      if(match(LEFT_BRACKET)) {
        if(findBracket) type.name += arrayType();
      }
    }

    public String arrayType() {
        String name = "[]";
        consume(RIGHT_BRACKET, "Expect ']' after array type");
        if(match(LEFT_BRACKET)) {
          name += arrayType();
        }
        return name;
    }

    public LoxType checkType(Modifiers modifiers, boolean findBracket) {
      for(TokenType t : types){
        if(check(t)) {
          LoxType type = new LoxType(peek(), modifiers);
          advance();
          expandType(type, findBracket);
          return type;
        }
      }
      return null;
    }

    public LoxType checkType(Modifiers modifiers)  {
      return checkType(modifiers, true);
    }

    public LoxType checkType() {
      return checkType(Modifiers.empty());
    }

    public LoxType checkType(boolean findBracket)  {
      return checkType(Modifiers.empty(), findBracket);
    }

    public LoxType matchType(Modifiers modifiers, boolean findBracket) {
      for(TokenType t : types){
        if(match(t)) {
          LoxType type = new LoxType(previous(), modifiers);
          expandType(type, findBracket);
          return type;
        }
      }
      return null;
    }

    public LoxType matchObjectType() {
      LoxType t = matchType(Modifiers.empty());
      if(t != null) {
        if(t.type != TypeEnum.OBJECT) {
          throw error(previous(), "Expected object type");
        }
      }
      return t;
    }

    public LoxType matchType() {
      return matchType(Modifiers.empty());
    }

    public LoxType matchType(Modifiers modifiers)  {
      return matchType(modifiers, true);
    }

    public LoxType matchType(boolean findBracket)  {
      return matchType(Modifiers.empty(), findBracket);
    }

    String getFuncPtr() {
      String name = "func(";
      consume(LEFT_PAREN, "expected function pointer type parameters");
      int numParams = 0;
      if(!check(RIGHT_PAREN)){
        do{
          numParams++;
          LoxType type = new LoxType(advance());
          if(type.type == TypeEnum.OBJECT)
          {
            if(match(FUN)) {
              type.name = getFuncPtr();
            }
            else{
              type.name = consume(IDENTIFIER, "Expect type name after 'obj'.").lexeme;
            }
          }
          name += type.name + ", ";
        } while (match(COMMA));
      }
      consume(RIGHT_PAREN, "Expect ')' after parameter types");
      consume(COLON, "Expect ':' after function pointer parameters");
      LoxType type = matchType(Modifiers.empty());
      if(type == null) {
        throw error(peek(), "Expected function pointer return type");
      }
      if(numParams > 0) name = name.substring(0, name.length() - 2);
      name += ")";
      name += ":"+type.name;
      return name;
    }

    private Expr.Typeof typeof(){
      consume(LEFT_PAREN, "Expect '(' after 'typeof'");
      Expr value = expression();
      consume(RIGHT_PAREN, "Expect ')' after expression");
      return new Expr.Typeof(value);
    }

    private Stmt classDeclaration(){
      Token name = consume(IDENTIFIER, "Expect class name");

      Expr.Variable superclass = null;
      List<Token> interfase = null;

      List<Token> types = new ArrayList<>();

      if(match(INARROW)){
        consume(IDENTIFIER, "Expect superclass name");
        superclass = new Expr.Variable(previous());
      }

      if(match(OUTARROW)) {
        interfase = new ArrayList<>();
        do{
          interfase.add(consume(IDENTIFIER, "Expect interface name"));
        } while(match(COMMA));
      }

      if(match(COLON)){
        consume(LESS, "Expect '<' after class name");
        do{
          types.add(consume(IDENTIFIER, "Expect type name"));
        } while(match(COMMA));
        consume(GREATER, "Expect '>' after type parameters");
      }
      
      consume(LEFT_BRACE, "Expect '{' before class body");

      List<Stmt.Function> methods = new ArrayList<>();
      List<Stmt.Var> variables = new ArrayList<>();
      List<Stmt.Property> properties = new ArrayList<>();
      while(!check(RIGHT_BRACE) && !isAtEnd()){
        LoxType rType = null;
        Modifiers modifiers = new Modifiers();
        while(matchModifier()){
          modifiers.add(previous().type);
        }
        LoxType type = null;
        if((type = checkType(modifiers)) != null){
          if(modifiers.contains(UNSIGNED) && type.type != TypeEnum.NUMBER){
            Token t = new Token(previous().type, "unsigned " + previous().lexeme, previous().literal, previous().line);
            throw error(t, "can only use unsigned modifier on numbers");
          }
          Token id = peek();
          advance();
          Expr expr = null;
          if(match(EQUAL)){
            expr = expression();
          }
          else if (match(LEFT_BRACE)) {
            Property p = property(type, modifiers, id);
            if(p != null) {
              properties.add(p);
              consume(RIGHT_BRACE, "Expect '}' after property declaration");
              continue;
            }
          }
          consume(SEMICOLON, "Expect semicolon after variable");
          variables.add(new Stmt.Var(id, expr, modifiers, type));
        }
        else if((check(METHOD))){
          advance();
          methods.add(function("method", modifiers));
        }
        else{
          error(peek(), "Expected method or variable");
        }
      }

      consume(RIGHT_BRACE, "Expect '}' after class body");

      return new Stmt.Class(name, superclass, methods, variables, properties, types, interfase);
    }

    public boolean matchModifier() {
      for(TokenType m : Modifiers.MODIFIERS) {
        if(match(m)) return true;
      }
      return false;
    }

    public Stmt.Interface interfaceDeclaration() {
      Token name = consume(IDENTIFIER, "Expect interface name"); 
      consume(LEFT_BRACE, "Expect '{' before interface body");

      List<Stmt.Function> methods = new ArrayList<>();
      List<Stmt.Var> variables = new ArrayList<>();
      while(!check(RIGHT_BRACE) && !isAtEnd()){
        Modifiers modifiers = new Modifiers();
        while(matchModifier()){
          modifiers.add(previous().type);
        }
        LoxType type = null;
        if((type = checkType(modifiers)) != null){
            advance();
            if(modifiers.contains(UNSIGNED) && type.type != TypeEnum.NUMBER){
              Token t = new Token(previous().type, "unsigned " + previous().lexeme, previous().literal, previous().line);
              throw error(t, "can only use unsigned modifier on numbers");
            }
            Token id = peek();
            advance();
            consume(EQUAL, "Expected equality");
            Expr expr = expression();
            consume(SEMICOLON, "Expect semicolon after variable");
            variables.add(new Stmt.Var(id, expr, modifiers, type));
          }
          else if(check(METHOD)){
            advance();
            methods.add(function("method", modifiers));
          }
          else if(check(OPERATOR)){
            advance();
            methods.add(function("method", modifiers));
          }
          else{
            error(peek(), "Expected method or variable");
          }
      }

      consume(RIGHT_BRACE, "Expect '}' after interface body");
      return new Stmt.Interface(name, methods, variables);
    }

    public Stmt.Enum enumDeclaration() {
      Token name = consume(IDENTIFIER, "Expect enum name");

      List<LoxEnum.Element> elements = new ArrayList<>();
      int step = 0;

      consume(LEFT_BRACE, "Expect '{' before enum body");

      if(!check(RIGHT_BRACE)){
        do{
          if(check(IDENTIFIER)){
            Token id = peek();
            advance();
            elements.add(new LoxEnum.Element(id, step));
            step++;
          }
          else{
            error(peek(), "Expected enum element");
          }
        } while(match(COMMA));
      }
      
      consume(RIGHT_BRACE, "Expect '}' after enum body");

      return new Stmt.Enum(name, elements);
    }

    private Stmt.Switch switchStatement() {

      consume(LEFT_PAREN, "Expect '(' after 'switch'");
      if(!check(IDENTIFIER)){
        error(peek(), "Expected identifier");
      }
      Expr id = expression();
      consume(RIGHT_PAREN, "Expect ')' after identifier");

      consume(LEFT_BRACE, "Expect '{' before switch body");

      List<Stmt.Case> cases = new ArrayList<>();
      Stmt.Case defaultCase = null;

      while(!check(RIGHT_BRACE) && !isAtEnd()){
        if(match(CASE)){
          consume(LEFT_PAREN, "Expect '(' before case value");
          Expr value = expression();
          consume(RIGHT_PAREN, "Expect ')' after case value");
          Stmt body = null;
          body = statement();
          cases.add(new Stmt.Case(value, body));
        }
        else if(match(DEFAULT)){
          Stmt body = null;
          body = statement();
          defaultCase = new Stmt.Case(null, body);
        }
      }

      consume(RIGHT_BRACE, "Expect '}' after switch body");

      return new Stmt.Switch(id, cases, defaultCase);
    }

    private Stmt.Function function(String kind, Modifiers modifiers){
      if(modifiers.contains(UNSIGNED)){
        throw error(previous(), "can only use unsigned modifier on numbers");
      }
      Token extClass = null;
      Token name = consume(IDENTIFIER, "Expect " + kind + " name or extension class.");
      if(match(COLON)){
          extClass = name;
          name = consume(IDENTIFIER, "Expect " + kind + " name.");
      }
      consume(LEFT_PAREN, "Expect '(' after " + kind + " name.");
      List<Parameter> parameters = new ArrayList<>();
      if(!check(RIGHT_PAREN)){
        do{
          if(parameters.size() >= 255){
            error(peek(), "Cant have more than 255 parameters");
          }
          LoxType type = matchType(modifiers);
          Token nameParam = consume(IDENTIFIER, "Expect parameter name.");
          parameters.add(new Parameter(nameParam, type));
        } while (match(COMMA));
      }
      consume(RIGHT_PAREN, "Expect ')' after parameters");

      consume(OUTARROW, "Expect '->' after parameters");

      LoxType type = matchType(modifiers);
      if(type == null){
        error(peek(), "Expected return type");
      }

      boolean hasBody = true;
      if(match(SEMICOLON)) hasBody = false;

      List<Stmt> body = null;
      if(hasBody){
        consume(LEFT_BRACE, "Expect '{' before " + kind + " body.");
        body = block();
      }
      
      return new Stmt.Function(name, extClass, parameters, body, modifiers,
       hasBody, type);
    }

    private Stmt varDeclaration(Modifiers modifiers, LoxType type){
      if(modifiers.contains(UNSIGNED) && type.type != TypeEnum.NUMBER){
        Token t = new Token(previous().type, "unsigned " + previous().lexeme, previous().literal, previous().line);
        throw error(t, "can only use unsigned modifier on numbers");
      }
      Token name = consume(IDENTIFIER, "Expect variable name.");
      Expr initializer = null;
      Stmt.Property property = null;
      if(match(EQUAL)){
        initializer = expression();
      }
      else if (match(LEFT_BRACE)) {
        property = property(type, modifiers, name);
      }
      if(property != null) {
        consume(RIGHT_BRACE, "Expect '}' after property declaration");
        return property;
      }
      else consume(SEMICOLON, "Expect ';' after variable declaration");
      
      return new Stmt.Var(name, initializer, modifiers, type);
    }

    private Stmt.Property property(LoxType type, Modifiers modifiers, Token name) {
      consume(GET, "Expect 'get' after '{'");
      consume(LEFT_BRACE, "Expect '{' after 'get'");
      List<Stmt> getter = block();
      Parameter value = new Parameter(new Token(TokenType.VALUE, "value", null, 0), type); 
      Stmt.Function getFunc = new Stmt.Function(new Token(IDENTIFIER, "get", null, previous().line), null, new ArrayList<>() {
        {
          add(value);
        }
      }, getter, new Modifiers(), true, type);
      Stmt.Function setFunc = null;
      if(match(SET)){
        consume(LEFT_BRACE, "Expect '{' after 'set'");
        List<Stmt> setter = block();
        Parameter sv = new Parameter(new Token(TokenType.VALUE, "value", null, 0), type); 
        setFunc = new Stmt.Function(new Token(IDENTIFIER, "set", null, previous().line), null, new ArrayList<>() {
          {
            add(sv);
          }
        }, setter, new Modifiers(), true, type);
      }

      return new Stmt.Property(name, modifiers, type, getFunc, setFunc);
    }

    private Stmt statement(){

      if(match(FOR)) return forStatement();
      if(match(IF)) return ifStatement();
      if(match(RETURN)) return returnStatement();
      if(match(EXPECT)) return expectStatement();
      if(match(WHILE)) return whileStatement();
      if(match(WHEN)) return whenStatement();
      if(match(TEST)) return testStatement();
      if(match(GETFILE)) return getFileStatement();
      if(match(MODULE)) return moduleStmt();
      if(match(SWITCH)) return switchStatement();
      if(match(BREAK)) return breakStatement();
      if(match(CONTINUE)) return continueStatement();
      if(match(TRY)) return tryStatement();
      
      if(match(LEFT_BRACE)) return new Stmt.Block(block());

      return expressionStatement();
    }

    private Stmt tryStatement() {
      Token ex = new Token(TokenType.IDENTIFIER, "error", null, current);
      Stmt tryBranch = statement();
      consume(CATCH, "Expect 'catch' after try statement");
      if(match(LEFT_PAREN)) {
        ex = consume(IDENTIFIER, "Expect exception name after catch");
        consume(RIGHT_PAREN, "Expect ')' after catch");
      }
      consume(LEFT_BRACE, "Expect '{' after catch");
      List<Stmt> catchBranch = block();
      return new Stmt.Try(tryBranch, catchBranch, ex);
    }

    private Stmt moduleStmt() {
      if(definedModule){
        throw error(previous(), "Module already defined");
      }
      definedModule = true;
      previous();
      while (!check(SEMICOLON)) {
        advance();
      }
      consume(SEMICOLON, "Expect ';' after module statement");
      return new Stmt.Module(previous());
    }

    private Stmt.Break breakStatement() {
      Token keyword = previous();
      consume(SEMICOLON, "Expect ';' after break");
      return new Stmt.Break(keyword);
    }

    private Stmt.Continue continueStatement() {
      Token keyword = previous();
      consume(SEMICOLON, "Expect ';' after continue");
      return new Stmt.Continue(keyword);
    }

    private Stmt getFileStatement() {
      Token keyword = previous();
      String impName = "";
      ArrayList<String> paths = new ArrayList<>();
      while(!check(SEMICOLON)){
        impName += advance().lexeme;
      }
      impName = impName.replace("\"", "");
      if(imports.contains(impName)){
        throw error(keyword, "Module '" + impName + "' already imported");
      }
      imports.add(impName);
      if(!impName.contains(".lox")) {
        File f = new File(impName);
        if(f.isDirectory()) {
          File[] listOfFiles = f.listFiles();
          for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
              if(listOfFiles[i].getName().contains(".lox")){
                  paths.add(listOfFiles[i].getPath());
              }
            }
          }
        }
        else{
          File folder = new File(".");
          File[] listOfFiles = folder.listFiles();
          for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
              if(listOfFiles[i].getName().contains(".lox")){
                if(hasModule(impName, listOfFiles[i])){
                  paths.add(listOfFiles[i].getPath());
                }
              }
            }
          }
        }
        //Get all .lox files in the directory
        
      }
      else{
        paths.add(impName);
      }
      
      consume(SEMICOLON, "Expect ';' after getfile statement");
      for(String path : paths){
        try{
          File f = new File(path);
          java.util.Scanner scanner = new java.util.Scanner(f);
          String file = "";
          while(scanner.hasNextLine()){
            String line = scanner.nextLine();
            if(line.contains("module") || line.contains("//")) continue;
            file += line;
          }
          List<Token> fileTokens = new Scanner(file).scanTokens();
          tokens.addAll(current, fileTokens);
          for(int i = 0; i < tokens.size(); i++){
            if(this.tokens.get(i).type == TokenType.EOF && i < this.tokens.size() - 1){
              this.tokens.remove(i);
              break;
            }
          }
          scanner.close();
          //tokens.forEach((tkn) -> {System.out.println(tkn.type);});
        }
        catch(Exception e){
          error(keyword, "File '" + path  + "' not found");
        }
      }
      
      //declaration();
      return new Stmt.GetFile(keyword, null);
    }

    boolean hasModule(String module, File file){
      try {
        try (java.util.Scanner scanner = new java.util.Scanner(file)) {
          if(scanner.hasNextLine()){
            String line = scanner.nextLine();
            if(line.contains(module) && line.contains("module")){
              scanner.close();
              return true;
            }
          }
        }
      } catch (FileNotFoundException e) {
        return false;
      }
      return false;
    }

    private Stmt returnStatement(){
      Token keyword = previous();
      Expr value = null;
      if(!check(SEMICOLON)){
        value = expression();
      }

      consume(SEMICOLON, "Expect ';' after return statement");
      return new Stmt.Return(keyword, value);
    }

    private Stmt expectStatement(){
      Token keyword = previous();
      Expr value = null;
      if(!check(SEMICOLON)){
        value = expression();
      }

      consume(SEMICOLON, "Expect ';' after expect statement");
      return new Stmt.Expect(keyword, value);
    }

    private Stmt forStatement(){
      consume(LEFT_PAREN, "Expect '(' after 'for'.");
      
      Stmt initializer;
      LoxType type = null;
      if(match(SEMICOLON)){
        initializer = null;
      }else if((type = matchType(Modifiers.empty())) != null){
        initializer = varDeclaration(new Modifiers(), type);
      } else{
        initializer = expressionStatement();
      }

      Expr condition = null;
      if(!check(SEMICOLON)){
        condition = expression();
      }
      consume(SEMICOLON, "Expect ';' after loop condition.");

      Expr increment = null;
      if(!check(RIGHT_PAREN)){
        increment = expression();
      }
      consume(RIGHT_PAREN, "Expect ')' after for clauses.");
      Stmt body = statement();

      if(increment != null){
        body = new Stmt.Block(
          Arrays.asList(
            body,
            new Stmt.Expression(increment)));
      }

      if(condition == null) condition = new Expr.Literal(true);
      body = new Stmt.While(condition, body);

      if(initializer != null){
        body = new Stmt.Block(Arrays.asList(initializer, body));
      }

      return body;
    }

    private Stmt whileStatement(){
      consume(LEFT_PAREN, "Expect '(' after 'while'.");
      Expr condition = expression();
      consume(RIGHT_PAREN, "Expect ')' after condition.");
      Stmt body = statement();

      return new Stmt.While(condition, body);
    }

    private Stmt ifStatement() {
      consume(LEFT_PAREN, "Expect '(' after 'if'");
      Expr condition = expression();
      consume(RIGHT_PAREN, "Expect ')' after condition");

      Stmt thenBranch = statement();
      Stmt elseBranch = null;
      if(match(ELSE)){
        elseBranch = statement();
      }

      return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt testStatement() {
      consume(LEFT_PAREN, "Expect '(' after 'test'");
      Expr condition = expression();
      consume(RIGHT_PAREN, "Expect ')' after condition");

      Stmt thenBranch = statement();

      return new Stmt.Test(condition, thenBranch);
    }

    private Stmt whenStatement() {
      consume(LEFT_PAREN, "Expect '(' after 'when'");
      Expr condition = expression();
      consume(RIGHT_PAREN, "Expect ')' after condition");

      Stmt thenBranch = statement();
      Stmt finallyBranch = null;
      if(match(FINALLY)){
        finallyBranch = statement();
      }

      return new Stmt.When(condition, thenBranch, finallyBranch);
    }

    private List<Stmt> block(){
      List<Stmt> statements = new ArrayList<>();

      while(!check(RIGHT_BRACE) && !isAtEnd()){
        statements.add(declaration());
      }

      consume(RIGHT_BRACE, "Expect '}' after block");
      return statements;
    }

    private Stmt expressionStatement() {
      Expr expr = expression();
      consume(SEMICOLON, "Expect ';' after expression.");
      return new Stmt.Expression(expr);
    }

    private Expr expression(){
      return newExpr();
    }

    private Expr assignment(){
      Expr expr = cast();

      if(match(EQUAL, PLUS_ASSIGN, INCREMENT, MINUS_ASSIGN, DECREMENT, STAR_ASSIGN, POWER)){
        AssignType type = AssignType.SET;
        switch (previous().type){
          case PLUS_ASSIGN:{
            type = AssignType.ADD;
            break;
          }
          case INCREMENT:{
            type = AssignType.INCREMENT;
            break;
          }
          case MINUS_ASSIGN:
            type = AssignType.SUBTRACT;
            break;
          case DECREMENT:
            type = AssignType.DECREMENT;
            break;
          case STAR_ASSIGN:
            type = AssignType.MULTIPLY;
            break;
          case POWER:
            type = AssignType.POWER;
            break;
          case SLASH_ASSIGN:
            type = AssignType.DIVIDE;
            break;
          default:
            break;
        }
        
        Token equals = previous();
        Expr value = assignment();

        if(expr instanceof Expr.Variable){
          Token name = ((Expr.Variable)expr).name;
          return new Expr.Assign(name, value, type);
        } else if(expr instanceof Expr.Get){
          Expr.Get get = ((Expr.Get)expr);
          return new Expr.Set(get.object, get.name, value);
        }
        else if(expr instanceof Expr.GetStatic){
          Expr.GetStatic get = ((Expr.GetStatic)expr);
          return new Expr.Set(get.object, get.name, value);
        }
        else if(expr instanceof Expr.GetIndex) {
          Expr.GetIndex get = ((Expr.GetIndex)expr);
          return new Expr.SetIndex(get.object, get.name, value, get.index);
        }

        error(equals, "Invalid assignment target");
      }
      // else if(match(SETASSIGN)) {
      //   Expr value = assignment();
      //   if(expr instanceof Expr.Variable){
      //     Token name = ((Expr.Variable)expr).name;
      //     return new Expr.SetAssign(name, value);
      //   } else if(expr instanceof Expr.Get){
      //     Expr.Get get = ((Expr.Get)expr);
      //     return new Expr.SetAssign(get.object, get.name, value);
      //   }
      //   else if(expr instanceof Expr.GetStatic){
      //     Expr.GetStatic get = ((Expr.GetStatic)expr);
      //     return new Expr.SetAssign(get.object, get.name, value);
      //   }
      // }
      return expr;
    }

    private Expr cast() {
      Expr expr = ternary();

      if(match(AS)){
        Token as = previous();
        Expr right = expression();
        return new Expr.Cast(as, expr, right);
      }

      return expr;
    }

    private Expr or(){
      Expr expr = and();

      while(match(OR)){
        Token operator = previous();
        Expr right = ternary();
        expr = new Expr.Logical(expr, operator, right);
      }

      return expr;
    }

    private Expr ternary() {
      Expr expr = or();

      if(match(QUESTION)){
        Token operToken = previous();
        Expr thenBranch = expression();
        consume(COLON, "Expect ':' after then branch of ternary");
        Expr elseBranch = expression();
        expr = new Expr.Ternary(operToken, expr, thenBranch, elseBranch);
      }

      return expr;
    }

    private Expr and(){
      Expr expr = is();

      while(match(AND)){
        Token operator = previous();
        Expr right = is();
        expr = new Expr.Logical(expr, operator, right);
      }

      return expr;
    }

    private Expr is() {
      Expr expr = equality();

      while(match(IS)){
        Token operator = previous();
        Expr right = expression();
        expr = new Expr.Logical(expr, operator, right);
      }

      return expr;
    }

    private Expr equality(){
      Expr expr = comparison();

      while (match(BANG_EQUAL, EQUAL_EQUAL, NULL_EQUAL, NULL_EQUAL_EQUAL)) {
        Token operator = previous();
        Expr right = comparison();
        expr = new Expr.Binary(expr, operator, right);
      }

      return expr;
    }

    private boolean match(TokenType... types){
      for(TokenType type : types){
        if(check(type)){
          advance();
          return true;
        }
      }

      return false;
    }

    private boolean check(TokenType type){
      if(isAtEnd()) return false;
      return peek().type == type;
    }

    private Token advance(){
      if(!isAtEnd()) current++;
      return previous();
    }

    private boolean isAtEnd(){
      return peek().type == EOF;
    }

    private Token peek(){
      return tokens.get(current);
    }

    private Token peekNext(){
      if(current + 1 >= tokens.size()) return null;
      return tokens.get(current + 1);
    }

    private Token previous(){
      return tokens.get(current-1);
    }

    private Expr comparison(){
      Expr expr = term();

      while(match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)){
        Token operator = previous();
        Expr right = term();
        expr = new Expr.Binary(expr, operator, right);
      }

      return expr;
    }

    private Expr term(){
      Expr expr = factor();

      while (match(MINUS, PLUS)){
        Token operator = previous();
        Expr right = factor();
        expr = new Expr.Binary(expr, operator, right);
      }

      return expr;
    }

    private Expr factor(){
      Expr expr = unary();

      while (match(SLASH, STAR)){
        Token operator = previous();
        Expr right = unary();
        expr = new Expr.Binary(expr, operator, right);
      }

      return expr;
    }

    private Expr unary(){
      if(match(BANG, MINUS)){
        Token operator = previous();
        Expr right = unary();
        return new Expr.Unary(operator, right);
      }

      return call(false, null, false);
    }


    private Expr call(boolean init, LoxType type, boolean foundIdentifier){
      Expr expr = primary(foundIdentifier);
      while(true){
        if(match(NULL_GET)){
          Token name = consume(IDENTIFIER, "Expect property name after '?.'");
          expr = new Expr.Coalesce(expr, name);
          if(match(LEFT_PAREN)){
            expr = finishCall(expr, true);
          }
        }
        else if(match(LEFT_PAREN)){
          expr = finishCall(expr, false);
        }
        else if(match(COLON)) {
          consume(LESS, "Expect '<' after ':'");
          List<Token> templates = new ArrayList<Token>();
          do{
            templates.add(consume(IDENTIFIER, "Expect type parameter name"));
          } while(match(COMMA));
          consume(GREATER, "Expect '>' after type parameters");
          consume(LEFT_PAREN, "Expect '(' after type parameters");
          expr = finishCall(expr, false, templates);
        }
        else if(match(DOT)){
          Token name = consume(IDENTIFIER, "Expect property name after '.'.");
          expr = new Expr.Get(expr, name);
        }
        else if(match(GETSTATIC)){
          Token name = consume(IDENTIFIER, "Expect function name after '::'.");
          expr = new Expr.GetStatic(expr, name);
        }
        else if(match(LEFT_BRACKET)) {
            Token name = previous();
            Expr index = expression();
            consume(RIGHT_BRACKET, "Expect ']' after index");
            expr = new Expr.GetIndex(expr, index, name);
        }
        else if(type != null && init) {
          consume(RIGHT_BRACKET, "Expect '[' after array type");
          return new Expr.Array(type, new ArrayList<Expr>(), expr);
        }
        else {
          break;
        }
      }

      return expr;
    }

    private Expr finishCall(Expr callee, boolean nullCheck){
      return finishCall(callee, nullCheck, new ArrayList<>());
    }

    private Expr finishCall(Expr callee, boolean nullCheck, List<Token> templates){
      List<Expr> arguments = new ArrayList<>();
      if(!check(RIGHT_PAREN)){
        do{
          if (arguments.size() >= 255) {
            error(peek(), "Can't have more than 255 arguments.");
          }
          arguments.add(expression());
        } while (match(COMMA));
      }

      Token paren = consume(RIGHT_PAREN,
                          "Expect ')' after arguments.");
      
      return new Expr.Call(callee, paren, arguments, nullCheck, templates);
    }

    private Expr primary(boolean fi){
      if(fi) {
        return new Expr.Variable(previous());
      }

      if(match(FALSE)) return new Expr.Literal(false);
      if(match(TRUE)) return new Expr.Literal(true);
      if(match(NIL)) return new Expr.Literal(null);

      if(match(NUMBER, STRING)){
        return new Expr.Literal(previous().literal);
      }

      if(match(SUPER)){
        Token keyword = previous();
        consume(DOT, "Expect '.' after 'super'");
        Token method = consume(IDENTIFIER,
          "Expect superclass method name.");
        return new Expr.Super(keyword, method);
      }

      if (match(THIS)) return new Expr.This(previous());
      if (match(VALUE)) return new Expr.Value(previous());

      if(match(IDENTIFIER)){
        return new Expr.Variable(previous());
      }

      if(match(ANONYMOUS)) {
        return anonymousFunction();
      }

      if(match(LEFT_PAREN)){
        Expr expr = expression();
        consume(RIGHT_PAREN, "Expect ')' after expression");
        return new Expr.Grouping(expr);
      }
      if(isSpecialAssign(previous().type)){
        return new Expr.Literal(0);
      }

      if(match(TYPEOF)){
        return typeof();
      }

      if(match(LENGTH)){
        return length();
      }

      LoxType lt = null;
      if((lt = checkAnon()) != null){
        return anonymousFunction(lt);
      }

      throw error(peek(), "Expect expression");
    }

    private Expr length() {
      Token name = previous();
      consume(LEFT_PAREN, "Expect '(' after 'length'");
      Expr value = expression();
      consume(RIGHT_PAREN, "Expect ')' after expression");
      return new Expr.Length(value, name);
    }

    private LoxType checkAnon() {
      LoxType t = checkType();
      TokenType next = peek().type;
      boolean b = t != null && next == LEFT_PAREN;
      return b ? t : null;
    }

    private Expr newExpr() {
      if(match(NEW)) {
        LoxType type = null;
        if((type = matchType(false)) != null) {
          return call(true, type, false);
        }
        else if(match(IDENTIFIER)) {
          return call(false, null, true);
        }
        if (type == null) {
          error(peek(), "Expected array type or object type");
        }
      }
      return anonymousFunction();
    }

    private Expr anonymousFunction(){
      LoxType ret = null;
      if((ret = matchType()) != null){
        if(check(LEFT_PAREN)){
            return anonymousFunction(ret);
        }
        return new Expr.Type(ret);
      }
      Expr expr = array();
      return expr;
    }

    private Expr array() {
      if(match(LEFT_BRACKET)) {
        List<Expr> elements = new ArrayList<>();
        LoxType type = matchType();
        if(type == null) {
          if(!check(RIGHT_BRACKET)){
            do{
              elements.add(expression());
            } while (match(COMMA));
          }
        }
        consume(RIGHT_BRACKET, "Expect ']' after array elements");
        return new Expr.Array(type, elements, null);
      }

      Expr expr = assignment();
      return expr;
    }

    private Expr anonymousFunction(LoxType ret) {
      consume(LEFT_PAREN, "Expect '(' after 'anonymous function'");
      List<Parameter> parameters = new ArrayList<>();
      if(!check(RIGHT_PAREN)){
        do{
          if(parameters.size() >= 255){
            error(peek(), "Cant have more than 255 parameters");
          }
          LoxType type = matchType(Modifiers.empty());
          Token nameParam = consume(IDENTIFIER, "Expect parameter name.");
          parameters.add(new Parameter(nameParam, type));
        } while (match(COMMA));
      }
      consume(RIGHT_PAREN, "Expect ')' after parameters");

      // consume(OUTARROW, "Expect '->' after parameters");

      // LoxType type = matchType();
      // if(type == null){
      //   error(peek(), "Expected return type");
      // }
      List<Stmt> body = null;
      consume(LEFT_BRACE, "Expect '{' before anonymous function body.");
      body = block();

      return new Expr.AnonymousFunction(parameters, body, ret);
    }

    public boolean isSpecialAssign(TokenType t){
      return t == TokenType.INCREMENT || t == TokenType.DECREMENT;
    }

    private Token consume(TokenType type, String message){
      if(check(type)) return advance();
      throw error(peek(), message);
    }

    private ParseError error(Token token, String message){
        Lox.error(token, message);
        throw new ParseError();
    }

    private void synchronize(){
      advance();

      while(!isAtEnd()){
        if (previous().type == SEMICOLON) return;

        switch (peek().type) {
          case CLASS:
          case FUN:
          case VAR:
          case FOR:
          case IF:
          case WHILE:
          case SWITCH:
          case WHEN:
          case TEST:
          case EXPECT:
          case RETURN:
            return;
          default:
            break;
        }

        advance();
      }
    }
}
