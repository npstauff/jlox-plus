package com.nix.lox;

import java.util.Map;

import javax.sound.sampled.AudioFileFormat.Type;

public class LoxType {

    public static enum TypeEnum {
        OBJECT,
        STRING,
        NUMBER,
        BOOLEAN,
        VOID,
        TYPE,
        ANY
    }

    public String name;
    public TypeEnum type;
    
    public LoxType(Token name) {
        this(name, new Modifiers());
    }

    public LoxType(TypeEnum type, String name) {
        this.name = name;
        this.type = type;
    }

    public LoxType(Token name, Modifiers modifiers) {
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
            case BYTE:
                this.type = TypeEnum.NUMBER;
                this.name = "num";
                modifiers.add(TokenType.BYTE);
                break;
            case TYPE:
                this.type = TypeEnum.TYPE;
                break;
            case ANY:
                this.type = TypeEnum.ANY;
                break;
            default:
                throw new RuntimeException("Invalid type " + name.type);
        }
    }

    public boolean isObject() {
        return type == TypeEnum.OBJECT;
    }

    public LoxType(String name, TypeEnum type) {
        this.name = name;
        this.type = type;
    }

    public LoxType(Object value) {
        if(value != null){
            if(value instanceof String || value instanceof Character){
                this.type = TypeEnum.STRING;
                this.name = "string";
            }
            else if(value instanceof Double || value instanceof Integer){
                this.type = TypeEnum.NUMBER;
                this.name = "num";
            }
            else if(value instanceof Boolean){
                this.type = TypeEnum.BOOLEAN;
                this.name = "bool";
            }
            else
                this.type = TypeEnum.OBJECT;

            if(value instanceof LoxType) {
                // this.name = ((LoxType)value).name;
                // this.type = ((LoxType)value).type;
                this.name = "type";
                this.type = TypeEnum.TYPE;
                return;
            }
            else if(value instanceof LoxClass) {
                name = ((LoxClass)value).name;
            }
            else if (value instanceof LoxInstance) {
                name = ((LoxInstance)value).klass.name;
            }
            else if(value instanceof LoxFunction) {
                String lexame = "func(";
                for(Parameter t : ((LoxFunction)value).declaration.params) {
                    lexame += t.type.name + ", ";
                }
                if(((LoxFunction)value).declaration.params.size() > 0) lexame = lexame.substring(0, lexame.length() - 2);
                name = lexame + "):"+((LoxFunction)value).declaration.returnType.name;
            }
            else if(value instanceof LoxProperty) {
                name = "property:" + ((LoxProperty)value).getType().name;
            }
            else if(value instanceof LoxArray) {
                name = ((LoxArray)value).getType().name;
                type = ((LoxArray)value).getType().type;
            }
            if(name == null) {
                this.name = "null";
            }
        } 
        else this.name = "null"; 
    }


    public boolean matches(LoxType other) {
        
        if(this.type == TypeEnum.ANY || other.type == TypeEnum.ANY) return true;
        if(this.type == TypeEnum.OBJECT && other.type == TypeEnum.OBJECT) {
            if(this.name == "null" || other.name == "null") return true;
            return this.name.equals(other.name);
        }
        if(this.type == TypeEnum.VOID){
            if(other.name == "null") return true;
        }
        else if(other.type == TypeEnum.VOID){
            if(this.name == "null") return true;
        }   
        return this.type == other.type && this.name.equals(other.name);
    }

    public boolean mismatch(LoxType other) {
        return !matches(other);
    }

    public String toString() {
        return type + " " + name;
    }
}
