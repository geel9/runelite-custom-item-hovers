package com.geel.customitemhovers.expressions.models;

public enum ArgumentType {
    ANY, // Literally any *valid argument type*
    PRIMITIVE, // Integer, double, string
    NUMERIC, // Integer, double
    INTEGER, // Only Integer
    DOUBLE, // Only Double
    STRING, // Only String
    LAMBDA, // Only Lambda
    GENERATOR, // Only Generator
    NAMESPACE; // Generator, Namespace (because all generators are namespaces)

    /**
     * Returns whether a given Token either:
     * - Fits this argument type OR
     * - Can be coerced to this argument type
     */
    public boolean fitsTokenType(TokenType type) {
        switch (this) {
            case ANY:
                return type.isValue();
            case PRIMITIVE:
                return type.isPrimitive();
            case NUMERIC:
            case INTEGER:
            case DOUBLE:
                return type == TokenType.INTEGER || type == TokenType.DOUBLE;
            case STRING:
                return type.isPrimitive();
            case LAMBDA:
                return type == TokenType.LAMBDA;
            case GENERATOR:
                return type == TokenType.GENERATOR;
            case NAMESPACE:
                return type == TokenType.NAMESPACE || type == TokenType.GENERATOR;
        }

        return false; //Should never get here
    }

    public Token coerceTokenToType(Token token) {
        if (!fitsTokenType(token.getType())) {
            throw new IllegalArgumentException("Cannot coerce token of invalid type");
        }

        TokenType type = token.getType();

        // Convert double to integer if needed
        if (this == INTEGER && type == TokenType.DOUBLE) {
            return Token.fromInt(token.getNumericValue().intValue());
        }

        // Convert integer to double if needed
        if (this == DOUBLE && type == TokenType.INTEGER) {
            return Token.fromDouble(token.getNumericValue().doubleValue());
        }

        // Convert integer or double to string if needed
        if (this == STRING) {
            if (token.getType() == TokenType.STRING) return token;
            return Token.fromString(token.getValue());
        }

        // No coercion needed for all other cases
        return token;
    }
}
