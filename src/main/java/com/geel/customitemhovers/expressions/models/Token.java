package com.geel.customitemhovers.expressions.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public class Token {
    @Getter
    private TokenType type;

    @Getter
    private String value; // TODO: Make more efficient than Strings. Are slices a thing in Java?

    @Getter
    @Setter
    private int numFunctionArgs = 0;

    public Token(TokenType type, String value) {
        this.type = type;
        this.value = value;
    }

    public Number getNumericValue() {
        if(type == TokenType.INTEGER) {
            return Integer.parseInt(value);
        } else if(type == TokenType.DOUBLE) {
            return Double.parseDouble(value);
        } else {
            throw new IllegalArgumentException("Not a number type");
        }
    }

    public static Token fromDouble(double value) {
        return new Token(TokenType.DOUBLE, String.valueOf(value));
    }

    public static Token fromInt(int value) {
        return new Token(TokenType.INTEGER, String.valueOf(value));
    }

    public static Token fromString(String value) {
        return new Token(TokenType.STRING, value);
    }

    public static Token fromBool(boolean value) {
        return new Token(TokenType.INTEGER, value ? "1" : "0");
    }

    public static Token fromTrue() {
        return fromBool(true);
    }

    public static Token fromFalse() {
        return fromBool(false);
    }
}
