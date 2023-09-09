package com.nix.lox;

import java.util.HashMap;
import java.util.Map;

public class LoxInterface {
    public final Map<String, FunctionTemplate> methods;
    public final Map<String, VarTemplate> fields;
    public final String name;

    public LoxInterface(String name, Map<String, FunctionTemplate> methods, Map<String, VarTemplate> fields){
        this.name = name;
        this.methods = methods;
        this.fields = fields;
    }
}
