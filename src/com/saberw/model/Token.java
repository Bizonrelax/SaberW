package com.saberw.model;

public class Token {
    public enum Type { WORD, SEPARATOR }
    
    private final String value;
    private final Type type;
    private final int position;
    
    public Token(String value, Type type, int position) {
        this.value = value;
        this.type = type;
        this.position = position;
    }
    
    public String getValue() { return value; }
    public Type getType() { return type; }
    public int getPosition() { return position; }
    public int length() { return value.length(); }
    
    public boolean isWord() { return type == Type.WORD; }
    public boolean isSeparator() { return type == Type.SEPARATOR; }
    
    @Override
    public String toString() {
        return String.format("Token[%s: '%s' @%d]", type, value, position);
    }
}