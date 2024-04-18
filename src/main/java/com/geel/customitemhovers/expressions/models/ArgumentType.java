package com.geel.customitemhovers.expressions.models;

public enum ArgumentType {
    ANY,
    INTEGER,
    DOUBLE,
    STRING;

    public boolean fitsTokenType(TokenType type) {
        if (!type.isConstant()) return false;
        if (this == ANY) return true;
        if (this == INTEGER || this == DOUBLE) return type == TokenType.INTEGER || type == TokenType.DOUBLE;
        if (this == STRING) return type == TokenType.STRING;

        return false; //Should never get here
    }

    public Token coerceTokenToType(Token token) {
        if(!fitsTokenType(token.getType())) {
            throw new IllegalArgumentException("Cannot coerce token of invalid type");
        }

        if(this == ANY) return token;
        if(this == INTEGER) return token.getType() == TokenType.INTEGER ? token : Token.fromInt(token.getNumericValue().intValue());
        if(this == DOUBLE) return token.getType() == TokenType.DOUBLE ? token : Token.fromDouble(token.getNumericValue().doubleValue());
        if(this == STRING){
            if(token.getType() == TokenType.STRING) return token;
            return Token.fromString(token.getValue());
        }

        return token; // Should not get here
    }
}
