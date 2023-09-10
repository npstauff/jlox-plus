package com.nix.lox;

public class Parameter {
    public Token name;
    public LoxType type;

    public Parameter(Token name, LoxType type){
        this.name = name;
        this.type = type;
    }
}
