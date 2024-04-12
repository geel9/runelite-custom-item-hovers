package com.geel.customitemhovers.expressions;

import lombok.Getter;

public enum TokenType {
    INTEGER(0),
    DOUBLE(0),
    VARIABLE(0),
    FUNCTION(5, true),
    OP_PLUS(1),
    OP_MINUS(1),
    OP_TIMES(2),
    OP_DIVIDE(2),
    PAREN_LEFT(4),
    PAREN_RIGHT(4),
    WHITESPACE(-1);

    @Getter
    private final int precedence;

    @Getter
    private final boolean rightAssociative;

    private TokenType(int precedence) {
        this.precedence = precedence;
        this.rightAssociative = false;
    }

    private TokenType(int precedence, boolean rightAssociative) {
        this.precedence = precedence;
        this.rightAssociative = rightAssociative;
    }

    public boolean isArithmetic() {
        return this == OP_PLUS || this == OP_MINUS;
    }

    public boolean isMultDiv() {
        return this == OP_TIMES || this == OP_DIVIDE;
    }

    public boolean isOperator() {
        return isArithmetic() || isMultDiv();
    }

    public boolean isParenthesis() {
        return this == PAREN_LEFT || this == PAREN_RIGHT;
    }

    public boolean isValue() {
        return isVariable() || isConstant();
    }

    public boolean isFunction() {
        return this == FUNCTION;
    }

    public boolean isVariable() {
        return this == VARIABLE;
    }

    public boolean isConstant() {
        return this == INTEGER || this == DOUBLE;
    }

    public boolean isIdentifier() {
        return isValue() || isFunction();
    }
}