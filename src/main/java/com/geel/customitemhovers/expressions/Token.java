package com.geel.customitemhovers.expressions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
class Token {
    @Getter
    private TokenType type;

    @Getter
    private String value; // TODO: Make more efficient than Strings. Are slices a thing in Java?

    public Number getConstantValue() {
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
}
