package com.nix.lox;

public class Break extends RuntimeException{
  Break(){
    super(null, null, false, false);
  }
}
