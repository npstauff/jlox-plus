package com.nix.lox;

public class LoxType {

    public static enum TypeEnum {
        OBJECT,
        STRING,
        NUMBER,
        BOOLEAN,
        VOID
    }

    public String name;
    public final TypeEnum type;
    
    public LoxType(Token name) {
        this.name = name.lexeme;
        switch (name.type) {
            case STRPARAM:
                this.type = TypeEnum.STRING;
                break;
            case NUMPARAM:
                this.type = TypeEnum.NUMBER;
                break;
            case BOOLEAN:
                this.type = TypeEnum.BOOLEAN;
                break;
            case VOID:
                this.type = TypeEnum.VOID;
                break;
            case OBJPARAM:
                this.type = TypeEnum.OBJECT;
                break;
            default:
                throw new RuntimeException("Invalid type " + name.type);
        }
    }

    public LoxType(String name, TypeEnum type) {
        this.name = name;
        this.type = type;
    }

    public LoxType(Object value) {
        if(value != null){
            if(value instanceof LoxClass) {
                name = ((LoxClass)value).name;
            }
            else if (value instanceof LoxInstance) {
                name = ((LoxInstance)value).klass.name;
            }
            else if(value instanceof LoxFunction) {
                name = "func";
            }
            else{
                name = value.getClass().getName();
            }
            if(name == null) {
                this.name = "null";
            }
        } 
        else this.name = "null";

        if(value instanceof String)
            this.type = TypeEnum.STRING;
        else if(value instanceof Double)
            this.type = TypeEnum.NUMBER;
        else if(value instanceof Boolean)
            this.type = TypeEnum.BOOLEAN;
        else
            this.type = TypeEnum.OBJECT;
    }


    public boolean matches(LoxType other) {
        if(this.type == TypeEnum.OBJECT && other.type == TypeEnum.OBJECT) {
            if(this.name == "null" || other.name == "null") return true;
            return this.name.equals(other.name);
        }   
        return this.type == other.type;
    }

    public boolean mismatch(LoxType other) {
        return !matches(other);
    }
}
