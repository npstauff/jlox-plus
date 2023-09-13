package com.nix.lox;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.nix.lox.LoxType.TypeEnum;
import static com.nix.lox.TokenType.*;

public class Parser {
    private static class ParseError extends RuntimeException {}


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
        if(matchType()) return varDeclaration(modifiers);

        return statement();
      } catch(ParseError e){
        synchronize();
        return null;
      }
    }

    public boolean checkType() {
        if(check(NUMPARAM) || check(STRPARAM) || check(BOOLEAN) || check(VOID) || check(OBJPARAM)) return true;
        else return false;
    }

    public boolean matchType() {
        if(match(NUMPARAM) || match(STRPARAM) || match(BOOLEAN) || match(VOID) || match(OBJPARAM)) return true;
        else return false;
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

      consume(LEFT_BRACE, "Expect '{' before class body");

      List<Stmt.Function> methods = new ArrayList<>();
      List<Stmt.Var> variables = new ArrayList<>();
      while(!check(RIGHT_BRACE) && !isAtEnd()){
        Modifiers modifiers = new Modifiers();
        while(matchModifier()){
          modifiers.add(previous().type);
        }
        if(checkType()){
            LoxType type = new LoxType(peek());
            if(type.type == TypeEnum.OBJECT) {
              advance();
              if(match(FUN)) {
                type.name = "func";
              }
              else{
                type.name = consume(IDENTIFIER, "Expect type name after obj").lexeme;
              }
            }
            else advance();

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

      consume(RIGHT_BRACE, "Expect '}' after class body");

      return new Stmt.Class(name, superclass, methods, variables, types, interfase);
    }

    public boolean matchModifier() {
      for(TokenType m : Modifiers.modifierTypes) {
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
        if(checkType()){
            LoxType type = new LoxType(peek());
            if(type.type == TypeEnum.OBJECT) {
              advance();
              if(match(FUN)) {
                type.name = "func";
              }
              else{
                type.name = consume(IDENTIFIER, "Expect type name after obj").lexeme;
              }
            }
            else advance();

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
      Token extClass = null;
      Token name = consume(IDENTIFIER, "Expect " + kind + " name or extension class.");
      if(match(CLASSEXT)){
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
          LoxType type = new LoxType(advance());
          if(type.type == TypeEnum.OBJECT)
          {
            if(peek().type == FUN) {
              advance();
              type.name = "func";
            }
            else{
              type.name = consume(IDENTIFIER, "Expect type name after 'obj'.").lexeme;
            }
          }
          Token nameParam = consume(IDENTIFIER, "Expect parameter name.");
          parameters.add(new Parameter(nameParam, type));
        } while (match(COMMA));
      }
      consume(RIGHT_PAREN, "Expect ')' after parameters");

      consume(OUTARROW, "Expect '->' after parameters");

      Token type = null;
      if(checkType()) {
        type = advance();
      }
      else{
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
       hasBody, new LoxType(type));
    }

    private Stmt varDeclaration(Modifiers modifiers){
      Token type = previous();
      LoxType lType = new LoxType(type);
      if(lType.type == TypeEnum.OBJECT) {
        if(match(FUN)){
          lType.name = "func";
        } 
        else {
          lType.name = consume(IDENTIFIER, "Expect type name after 'obj'.").lexeme;
        }
      }
      Token name = consume(IDENTIFIER, "Expect variable name.");
      Expr initializer = null;
      if(match(EQUAL)){
        initializer = expression();
      }

      consume(SEMICOLON, "Expect ';' after variable declaration");
      return new Stmt.Var(name, initializer,modifiers, lType);
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
      
      if(match(LEFT_BRACE)) return new Stmt.Block(block());

      return expressionStatement();
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
      if(match(SEMICOLON)){
        initializer = null;
      }else if(match(VAR)){
        initializer = varDeclaration(new Modifiers());
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
      return assignment();
    }

    private Expr assignment(){
      Expr expr = or();

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

        error(equals, "Invalid assignment target");
      }

      return expr;
    }

    private Expr or(){
      Expr expr = and();

      while(match(OR)){
        Token operator = previous();
        Expr right = and();
        expr = new Expr.Logical(expr, operator, right);
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
        Expr right = equality();
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

      return call();
    }


    private Expr call(){
      Expr expr = primary();

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
        } else if(match(DOT)){
          Token name = consume(IDENTIFIER, "Expect property name after '.'.");
          expr = new Expr.Get(expr, name);
        }
        else if(match(GETSTATIC)){
          Token name = consume(IDENTIFIER, "Expect function name after '::'.");
          expr = new Expr.GetStatic(expr, name);
        } 
        else {
          break;
        }
      }

      return expr;
    }

    private Expr finishCall(Expr callee, boolean nullCheck){
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
      
      return new Expr.Call(callee, paren, arguments, nullCheck);
    }

    private Expr primary(){
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

      if(match(IDENTIFIER)){
        return new Expr.Variable(previous());
      }

      if(match(LEFT_PAREN)){
        Expr expr = expression();
        consume(RIGHT_PAREN, "Expect ')' after expression");
        return new Expr.Grouping(expr);
      }
      if(isSpecialAssign(previous().type)){
        return new Expr.Literal(0);
      }

      if(match(NEW)){
        return call();
      }

      if(match(TYPEOF)){
        return typeof();
      }

      throw error(peek(), "Expect expression");
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
        return new ParseError();
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
