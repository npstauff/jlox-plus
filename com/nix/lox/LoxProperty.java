package com.nix.lox;

import java.util.ArrayList;

public class LoxProperty {
    private final LoxFunction get;
    private final LoxFunction set;
    private final Token name;
    private final LoxType type;
    private final Modifiers modifiers;
    public Object value = null;

    public LoxProperty(Token name, Modifiers modifiers, LoxType type, LoxFunction get, LoxFunction set) {
        this.name = name;
        this.modifiers = modifiers;
        this.type = type;
        this.get = get;
        this.set = set;
    }


    public LoxFunction getGet() {
        return get;
    }

    public LoxFunction getSet() {
        return set;
    }

    public Token getName() {
        return name;
    }

    public LoxType getType() {
        return type;
    }

    public Modifiers getModifiers() {
        return modifiers;
    }

    public Object get(Interpreter interpreter) {
        ArrayList<Object> args = new ArrayList<>();
        args.add(value);
        return get.call(interpreter, args, new ArrayList<>());
    }

    public Object set(Interpreter interpreter, ArrayList<Object> args) {
        return set.call(interpreter, args, new ArrayList<>());
    }

}
