package com.nix.lox;

import java.util.List;

public class VarTemplate {
    public Token name;
    public boolean isStatic, isConstant;

    public VarTemplate(Token name, boolean isStatic, boolean isConstant){
        this.name = name;
        this.isStatic = isStatic;
        this.isConstant = isConstant;
    }
}
