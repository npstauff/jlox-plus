package com.nix.lox;

import java.util.List;

public class FunctionTemplate {
    public Token name;
    public boolean isStatic, isConstant, isoperator;
    public List<Token> params;
    public Boolean hasBody;
    public List<Stmt> body;

    public FunctionTemplate(Token name, List<Token> params, boolean isStatic, boolean isConstant, Boolean hasBody, List<Stmt> body, Boolean isoperator){
        this.name = name;
        this.params = params;
        this.isStatic = isStatic;
        this.isConstant = isConstant;
        this.hasBody = hasBody;
        this.body = body;
        this.isoperator = isoperator;
    }
}
