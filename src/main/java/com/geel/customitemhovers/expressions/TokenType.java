package com.geel.customitemhovers.expressions;

import lombok.Getter;

enum TokenType {
    // Order of operations:
    // Parentheses/Functions (0), Multiplication/Division (1), Arithmetic (2), Constants/Variables (3),
    // Range Comparators (<, <=, >, >=) (4), Equality Comparators (==, !=) (5), Logical && (6), Logical || (7),
    // Whitespace (8)
    //
    //PFMDACVRCECLALOW :)

    PAREN_LEFT(0, "("),
    PAREN_RIGHT(0, ")"),
    FUNCTION(0, null, true),
    OP_TIMES(1, "*"),
    OP_DIVIDE(1, "/"),
    OP_MODULO(1, "%"),
    OP_PLUS(2, "+"),
    OP_MINUS(2, "-"),
    INTEGER(3, null),
    DOUBLE(3, null),
    VARIABLE(3, null),
    CMP_LT(4, "<"),
    CMP_LTE(4, "<="),
    CMP_GT(4, ">"),
    CMP_GTE(4, ">="),
    CMP_EQUALS(5, "=="),
    CMP_NEQUALS(5, "!="),
    LOG_AND(6, "&&"),
    LOG_OR(7, "||"),
    WHITESPACE(8, null); // Not a real token

    @Getter
    private final int precedence;

    @Getter
    private final boolean rightAssociative;

    @Getter
    private final String constantValue;

    private TokenType(int precedence, String constantValue) {
        this.precedence = precedence;
        this.constantValue = constantValue;
        this.rightAssociative = false;
    }

    private TokenType(int precedence, String constantValue,  boolean rightAssociative) {
        this.precedence = precedence;
        this.constantValue = constantValue;
        this.rightAssociative = rightAssociative;
    }

    private static TokenType[] operatorTokens = new TokenType[] {
            OP_TIMES, OP_DIVIDE, OP_MODULO, OP_MINUS, OP_PLUS,
            CMP_LT, CMP_LTE, CMP_GT, CMP_GTE, CMP_EQUALS, CMP_NEQUALS,
            LOG_AND, LOG_OR
    };

    public static TokenType[] GetOperatorTokenTypes() {
        return operatorTokens;
    }

    public boolean hasFixedText() {
        return this.constantValue != null;
    }

    public boolean isArithmetic() {
        return this == OP_PLUS || this == OP_MINUS;
    }

    public boolean isMultDiv() {
        return this == OP_TIMES || this == OP_DIVIDE || this == OP_MODULO;
    }

    public boolean isLogicalOperator() {
        return this == LOG_AND || this == LOG_OR;
    }

    public boolean isComparatorOperator() {
        return this == CMP_LT || this == CMP_GT || this == CMP_LTE || this == CMP_GTE || this == CMP_EQUALS || this == CMP_NEQUALS;
    }

    public boolean isBooleanOperator() {
        return isLogicalOperator() || isComparatorOperator();
    }

    public boolean isOperator() {
        return isArithmetic() || isMultDiv() || isLogicalOperator() || isComparatorOperator();
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