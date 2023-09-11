package com.nix.lox;

import java.util.List;

public class VarTemplate {
    public Token name;
    public boolean isStatic, isConstant;
    public LoxType type;

    public VarTemplate(Token name, boolean isStatic, boolean isConstant, LoxType type){
        this.name = name;
        this.isStatic = isStatic;
        this.isConstant = isConstant;
        this.type = type;
    }
}
