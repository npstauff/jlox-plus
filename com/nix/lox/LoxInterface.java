package com.nix.lox;

import java.util.Map;

public class LoxInterface {
    public final Map<String, Stmt.Function> methods;
    public final Map<String, Stmt.Var> fields;
    public final String name;

    public LoxInterface(String name, Map<String, Stmt.Function> methods, Map<String, Stmt.Var> fields){
        this.name = name;
        this.methods = methods;
        this.fields = fields;
    }
}
