package com.nix.lox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.print.DocFlavor.STRING;

import com.nix.lox.Stmt.While;

import static com.nix.lox.TokenType.*;

public class Parser {
    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
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
        if(match(CONST)){
          if(match(FUN)){
            return function("const method");
          }
          else{
            return varDeclaration(true);
          }
        } 
        if(match(FUN)) return function("function");
        if(match(VAR)) return varDeclaration(false);

        return statement();
      } catch(ParseError e){
        synchronize();
        return null;
      }
    }

    private Stmt classDeclaration(){
      Token name = consume(IDENTIFIER, "Expect class name");

      Expr.Variable superclass = null;

      if(match(LESS)){
        consume(IDENTIFIER, "Expect superclass name");
        superclass = new Expr.Variable(previous());
      }

      consume(LEFT_BRACE, "Expect '{' before class body");

      List<Stmt.Function> methods = new ArrayList<>();
      List<Stmt.Var> variables = new ArrayList<>();
      while(!check(RIGHT_BRACE) && !isAtEnd()){
        if(check(STATIC)){
          advance();
          methods.add(function("static method"));
        }
        else if(check(CONST)){
          advance();
          if(check(IDENTIFIER)){
            Token id = peek();
            advance();
            Token eq = consume(EQUAL, "Expected equality");
            Expr expr = expression();
            Token semicolen = consume(SEMICOLON, "Expect semicolon after variable");
            variables.add(new Stmt.Var(id, expr, true));
          }
          else{
            methods.add(function("const method"));
          }
        }
        else if(check(VAR)){
          advance();
          if(check(IDENTIFIER)){
            Token id = peek();
            advance();
            consume(EQUAL, "Expected equality");
            Expr expr = expression();
            consume(SEMICOLON, "Expect semicolon after variable");
            variables.add(new Stmt.Var(id, expr, false));
          }
        }
        else{
          if(previous().type != CONST) methods.add(function("method"));
        }
      }

      consume(RIGHT_BRACE, "Expect '}' after class body");

      return new Stmt.Class(name, superclass, methods, variables);
    }

    private Stmt.Function function(String kind){
      Token name = consume(IDENTIFIER, "Expect " + kind + " name.");
      consume(LEFT_PAREN, "Expect '(' after " + kind + " name.");
      List<Token> parameters = new ArrayList<>();
      if(!check(RIGHT_PAREN)){
        do{
          if(parameters.size() >= 255){
            error(peek(), "Cant have more than 255 parameters");
          }

          parameters.add(
            consume(IDENTIFIER, "Expect parameter name."));
        } while (match(COMMA));
      }
      consume(RIGHT_PAREN, "Expect ')' after parameters");

      consume(LEFT_BRACE, "Expect '{' before " + kind + " body.");
      List<Stmt> body = block();
      return new Stmt.Function(name, parameters, body, kind.equals("static method"), kind.equals("const method"));
    }

    private Stmt varDeclaration(boolean constant){
      Token name = consume(IDENTIFIER, "Expect variable name.");

      Expr initializer = null;
      if(match(EQUAL)){
        initializer = expression();
      }

      consume(SEMICOLON, "Expect ';' after variable declaration");
      return new Stmt.Var(name, initializer, constant);
    }

    private Stmt statement(){

      if(match(FOR)) return forStatement();
      if(match(IF)) return ifStatement();
      if(match(RETURN)) return returnStatement();
      if(match(WHILE)) return whileStatement();
      if(match(WHEN)) return whenStatement();
      if(match(LEFT_BRACE)) return new Stmt.Block(block());

      return expressionStatement();
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

    private Stmt forStatement(){
      consume(LEFT_PAREN, "Expect '(' after 'for'.");
      
      Stmt initializer;
      if(match(SEMICOLON)){
        initializer = null;
      }else if(match(VAR)){
        initializer = varDeclaration(false);
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
      Expr expr = equality();

      while(match(AND)){
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
          case PRINT:
          case RETURN:
            return;
        }

        advance();
      }
    }
}
