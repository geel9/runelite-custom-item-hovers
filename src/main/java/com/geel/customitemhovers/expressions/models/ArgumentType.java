package com.geel.customitemhovers.expressions.models;

public enum ArgumentType {
    ANY, // TODO: doesn't include expressions and shouldn't, but confusing name
    INTEGER,
    DOUBLE,
    STRING,
    LAMBDA;

    public boolean fitsTokenType(TokenType type) {
        if (this == ANY) return type.isConstant();
        if (this == INTEGER || this == DOUBLE) return type == TokenType.INTEGER || type == TokenType.DOUBLE;
        if (this == STRING) return type == TokenType.STRING;
        if (this == LAMBDA) return type == TokenType.LAMBDA;

        return false; //Should never get here
    }

    public Token coerceTokenToType(Token token) {
        if (!fitsTokenType(token.getType())) {
            throw new IllegalArgumentException("Cannot coerce token of invalid type");
        }

        if (this == ANY || this == LAMBDA) return token;
        if (this == INTEGER)
            return token.getType() == TokenType.INTEGER ? token : Token.fromInt(token.getNumericValue().intValue());
        if (this == DOUBLE)
            return token.getType() == TokenType.DOUBLE ? token : Token.fromDouble(token.getNumericValue().doubleValue());
        if (this == STRING) {
            if (token.getType() == TokenType.STRING) return token;
            return Token.fromString(token.getValue());
        }

        return token; // Should not get here
    }
}
