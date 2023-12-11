package com.nix.lox;

public class LoxArray {
    private LoxType type;
    private Object[] array;
    private int size;
    private Token name = new Token(TokenType.EOF, "null", null, 0);

    public LoxArray(LoxType type, int size){
        this.type = type;
        this.size = size;
        array = new Object[size];
    }

    public Object[] values() {
        return array;
    }

    public LoxType getType(){
        return type;
    }

    public Object get(int index){
        checkSize(index);
        return array[index];
    }

    public void checkSize(int index){
        if(index >= size) throw new RuntimeError(name, "Index " + index + " out of bounds for array of size " + size);
    }

    public void set(int index, Object value){
        LoxType baseType = new LoxType(type.type, type.name);
        baseType.name = baseType.name.substring(0, baseType.name.length() - 2);
        checkSize(index);
        if(value != null) {
            if(baseType.mismatch(new LoxType(value))) {
                throw new RuntimeError(name, "Type mismatch for array of type '" + type + "' and '" + new LoxType(value) + "'");
            }
        }
        array[index] = value;
    }

    public int getSize(){
        return size;
    }

    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for(int i = 0; i < size; i++){
            builder.append(array[i]);
            if(i != size - 1) builder.append(", ");
        }
        builder.append("]");
        return builder.toString();
    }
}
