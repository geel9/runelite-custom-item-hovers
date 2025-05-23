package com.geel.customitemhovers.expressions.models;

import com.geel.customitemhovers.expressions.generators.Generator;
import com.geel.customitemhovers.expressions.providers.INamespace;
import lombok.Getter;
import lombok.Setter;

public class Token {
    @Getter
    private TokenType type;

    @Getter
    private String value; // TODO: Make more efficient than Strings. Are slices a thing in Java?

    private Number numericValue;

    @Getter
    private INamespace namespaceValue;

    @Getter
    @Setter
    private int numFunctionArgs = 0;

    @Getter
    @Setter
    private String[] lambdaArgs = null;

    // HACKY
    @Getter
    @Setter
    private TokenizedExpression tokenizedLambda = null;

    @Getter
    @Setter
    private CompiledExpression compiledLambda = null;

    public Token(TokenType type, String value) {
        this.type = type;
        this.value = value;
    }

    private Token(int value) {
        this(TokenType.INTEGER, String.valueOf(value));

        this.numericValue = value;
    }

    private Token(double value) {
        this(TokenType.DOUBLE, String.valueOf(value));

        this.numericValue = value;
    }

    public Number getNumericValue() {
        if (numericValue != null) {
            return numericValue;
        }

        if (type == TokenType.INTEGER) {
            return numericValue = Integer.parseInt(value);
        } else if (type == TokenType.DOUBLE) {
            return numericValue = Double.parseDouble(value);
        } else {
            throw new IllegalArgumentException("Not a number type");
        }
    }

    public boolean getBooleanValue() {
        if (type == TokenType.INTEGER) {
            return getNumericValue().intValue() != 0;
        } else if (type == TokenType.DOUBLE) {
            return Math.abs(getNumericValue().doubleValue()) > 0.0001;
        } else {
            throw new IllegalArgumentException("Not a boolean type");
        }
    }

    public static Token fromDouble(double value) {
        return new Token(TokenType.DOUBLE, String.valueOf(value));
    }

    public static Token fromInt(int value) {
        return new Token(value);
    }

    public static Token fromString(String value) {
        return new Token(TokenType.STRING, value);
    }

    public static Token fromBool(boolean value) {
        return new Token(value ? 1 : 0);
    }

    public static Token fromTrue() {
        return fromBool(true);
    }

    public static Token fromFalse() {
        return fromBool(false);
    }

    public static Token fromNamespace(INamespace namespace) {
        Token token = new Token(TokenType.NAMESPACE, null);
        token.namespaceValue = namespace;
        return token;
    }

    public static Token fromGenerator(Generator generator) {
        Token token = new Token(TokenType.GENERATOR, null);
        token.namespaceValue = generator;
        return token;
    }
}
