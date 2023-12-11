package com.nix.lox;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Modifiers {

    private List<TokenType> modifiers;

    public static TokenType[] MODIFIERS = {TokenType.CONST, TokenType.STATIC, TokenType.OPERATOR, TokenType.CAST, TokenType.UNSIGNED};

    public Modifiers(List<TokenType> modifiers){
        this.modifiers = modifiers;
    }

    public Modifiers(){
        this(new ArrayList<TokenType>());
    }

    public Modifiers(TokenType... modifiers){
        this(Arrays.asList(modifiers));
    }

    public boolean contains(TokenType modifier){
        return modifiers.contains(modifier);
    }

    public List<TokenType> getModifiers(){
        return modifiers;
    }

    public void add(TokenType modifier){
        modifiers.add(modifier);
    }

    public void add(TokenType modifier, boolean shouldAdd){
        if(!shouldAdd) return;
        add(modifier);
    }

    public void remove(TokenType modifier){
        modifiers.remove(modifier);
    }

    public int size(){
        return modifiers.size();
    }

    public static Modifiers empty() {
        return new Modifiers();
    }

    public boolean matches(Modifiers other){
        if(other == null) return false;
        if(other.size() != size()) return false;
        for(TokenType type : MODIFIERS){
            if(contains(type) != other.contains(type)) return false;
        }
        return true;
    }

    public boolean mismatch(Modifiers other){
        return !matches(other);
    }

    public boolean not(TokenType t) {
        return !contains(t);
    }
}
