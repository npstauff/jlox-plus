package com.nix.lox;

import java.util.Map;

/**
 * LoxEnum
 */
public class LoxEnum {
    public final Token name;
    public final Map<Token, LoxEnum.Element> elements;

    public LoxEnum(Token name, Map<Token, LoxEnum.Element> elements){
        this.name = name;
        this.elements = elements;
    }

    public int getValue(Token name){
        for(Token entry : elements.keySet()){
            if(entry.lexeme.equals(name.lexeme))
                return elements.get(entry).value;
        }
        throw new RuntimeException("Enum " + this.name.lexeme + " does not contain element " + name.lexeme);
    }

    void printElements(){
        System.out.println("Enum " + name.lexeme + " contains:");
        for(Map.Entry<Token, LoxEnum.Element> entry : elements.entrySet()){
            System.out.println(entry.getKey().lexeme + " = " + entry.getValue().value);
        }
    }

    static class Element{
        public Token name;
        public int value;

        public Element(Token name, int value){
            this.name = name;
            this.value = value;
        }
    }
}

