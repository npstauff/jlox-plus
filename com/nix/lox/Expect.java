package com.nix.lox;

public class Expect extends RuntimeException{
  final Object value;

  Expect(Object value){
    super(null, null, false, false);
    this.value = value;
  }
}
