package com.nix.lox;

public class Continue extends RuntimeException{
  Continue(){
    super(null, null, false, false);
  }
}
