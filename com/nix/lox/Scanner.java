package com.nix.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {
  private final String source;
  private final List<Token> tokens = new ArrayList<>();

  private int start = 0;
  private int current = 0;
  private int line = 1;

  private static final Map<String, TokenType> keywords;

  static{
    keywords = new HashMap<>();
    keywords.put("and",    TokenType.AND);
    keywords.put("class",  TokenType.CLASS);
    keywords.put("else",   TokenType.ELSE);
    keywords.put("false",  TokenType.FALSE);
    keywords.put("for",    TokenType.FOR);
    keywords.put("fun",    TokenType.FUN);
    keywords.put("if",     TokenType.IF);
    keywords.put("nil",    TokenType.NIL);
    keywords.put("or",     TokenType.OR);
    keywords.put("return", TokenType.RETURN);
    keywords.put("super",  TokenType.SUPER);
    keywords.put("this",   TokenType.THIS);
    keywords.put("true",   TokenType.TRUE);
    keywords.put("var",    TokenType.VAR);
    keywords.put("while",  TokenType.WHILE);
    keywords.put("when",  TokenType.WHEN);
    keywords.put("do",  TokenType.FINALLY);
    keywords.put("common",  TokenType.STATIC);
    keywords.put("const",  TokenType.CONST);
  }

  Scanner(String source){
    this.source = source;
  }

  List<Token> scanTokens(){
    while(!isAtEnd()){
      start = current;
      scanToken();
    }

    tokens.add(new Token(TokenType.EOF, "", null, line));
    return tokens;
  }

  private void scanToken(){
    char c = advance();
    switch(c){
      case '(': addToken(TokenType.LEFT_PAREN); break;
      case ')': addToken(TokenType.RIGHT_PAREN); break;
      case '{': addToken(TokenType.LEFT_BRACE); break;
      case '}': addToken(TokenType.RIGHT_BRACE); break;
      case ',': addToken(TokenType.COMMA); break;
      case '.': addToken(TokenType.DOT); break;
      case '-':{
        if(match('=')){
          addToken(TokenType.MINUS_ASSIGN);
        }
        else if(match('-')){
          addToken(TokenType.DECREMENT);
        }
        else{
          addToken(TokenType.MINUS);
        }
      } break;
      case '+':{
        if(match('=')){
          addToken(TokenType.PLUS_ASSIGN);
        }
        else if(match('+')){
          addToken(TokenType.INCREMENT);
        }
        else{
          addToken(TokenType.PLUS);
        }
      } break;
      case ';': addToken(TokenType.SEMICOLON); break;
      case '*':{
        if(match('=')){
          addToken(TokenType.STAR_ASSIGN);
        }
        else if(match('*')){
          addToken(TokenType.POWER);
        }
        else{
          addToken(TokenType.STAR);
        }
      } break;
      case '?':
        if(match('.')){
          addToken(TokenType.NULL_GET);
        }
        else if(match('=')){
          addToken(TokenType.NULL_EQUAL);
        }
        else if(match('?')){
          addToken(TokenType.NULL_EQUAL_EQUAL);
        }
        else{
          Lox.error(new Token(TokenType.FALSE, "?", null, line), "Expect '?' or '=' or '.' after a null safe expression");
        }
       break;
      case '!':
        addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
        break;
      case '=':
        addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
        break;
      case '<':
        addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
        break;
      case '>':
        addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
        break;
      case '/':
        if(match('/')){
          while (peek() != '\n' && !isAtEnd()) advance();
        } else{
          addToken(TokenType.SLASH);
        }
        break;
      case ' ':
      case '\r':
      case '\t':
        // Ignore whitespace.
        break;
      case '\n':
        line++;
        break;
      case '"': string(); break;

      default:
        if(isDigit(c)){
          number();
        }
        else if(isAlpha(c)){
          identifier();
        } 
        else{
          Lox.error(line, "Unexpected character.");
        }
        break;
    }
  }

  private void identifier(){
    while(isAlphaNumeric(peek())) advance();

    String text = source.substring(start, current);
    TokenType type = keywords.get(text);
    if(type == null) type = TokenType.IDENTIFIER;
    addToken(type);
  }

  private boolean isAlpha(char c){
    return (c >= 'a' && c <= 'z') ||
           (c >= 'A' && c <= 'Z') ||
            c == '_';
  }

  private boolean isAlphaNumeric(char c){
    return isAlpha(c) || isDigit(c);
  }

  private void number(){
    while(isDigit(peek())) advance();

    if(peek() == '.' && isDigit(peekNext())){
      advance();

      while(isDigit(peek())) advance();
    }

    addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
  }

  private void string(){
    while (peek() != '"' && !isAtEnd()){
      if(peek() == '\n') line++;
      advance();
    }

    if(isAtEnd()){
      Lox.error(line, "Unterminated string");
      return;
    }

    advance();

    String value = source.substring(start+1, current-1);
    addToken(TokenType.STRING, value);
  }

  private boolean match(char expected) {
    if(isAtEnd()) return false;
    if(source.charAt(current) != expected) return false;

    current++;
    return true;
  }

  private char peek(){
    if(isAtEnd()) return '\0';
    return source.charAt(current);
  }

  private char peekNext(){
    if(current + 1 >= source.length()) return '\0';
    return source.charAt(current + 1);
  }

  private boolean isDigit(char c){
    return c >= '0' && c <= '9';
  }

  private boolean isAtEnd(){
    return current >= source.length();
  }

  private char advance(){
    return source.charAt(current++);
  }

  private void addToken(TokenType type){
    addToken(type, null);
  }

  private void addToken(TokenType type, Object literal){
    String text = source.substring(start, current);
    tokens.add(new Token(type, text, literal, line));
  }
}