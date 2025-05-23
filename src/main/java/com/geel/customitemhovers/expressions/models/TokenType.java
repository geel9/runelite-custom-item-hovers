package com.geel.customitemhovers.expressions.models;

import lombok.Getter;

public enum TokenType {
    // Order of operations:
    // Parentheses/Functions (0), Multiplication/Division (1), Arithmetic (2), Constants/Variables (3),
    // Bitshifts (<<, >>) (4), Range Comparators (<, <=, >, >=) (5), Equality Comparators (==, !=) (6),
    // Bitwise AND (&) (7), Bitwise XOR (^) (8), Bitwise OR (|) (9),
    // Logical && (10), Logical || (11),
    // Ternary Then (?) (12), Ternary Else (:) (13),
    // Comma (,) (14),
    // Expression begin/end (${/<%, }/%>) (15)
    // Whitespace (16)

    PAREN_LEFT(0, "("),
    PAREN_RIGHT(0, ")"),
    FUNCTION(0, null, true),
    OP_TIMES(1, "*"),
    OP_DIVIDE(1, "/"),
    OP_MODULO(1, "%"),
    OP_PLUS(2, "+"),
    OP_MINUS(2, "-"),
    STRING(3, null),
    INTEGER(3, null),
    DOUBLE(3, null),
    VARIABLE(3, null),
    LAMBDA(3, null), // Produced by the Compiler
    NAMESPACE(3, null), // Produced by the Compiler; an "object"
    NAMESPACE_ACCESSOR(3, ".", true), // Produced by the Compiler; an "object" member access
    GENERATOR(3, null), // Produced by the Compiler; a "generator"
    BITSHIFT_LEFT(4, "<<"),
    BITSHIFT_RIGHT(4, ">>"),
    CMP_LT(5, "<"),
    CMP_LTE(5, "<="),
    CMP_GT(5, ">"),
    CMP_GTE(5, ">="),
    CMP_EQUALS(6, "=="),
    CMP_NEQUALS(6, "!="),
    BIT_AND(7, "&"),
    BIT_XOR(8, "^"),
    BIT_OR(9, "|"),
    LOG_AND(10, "&&"),
    LOG_OR(11, "||"),
    TERNARY_THEN(12, "?", true),
    TERNARY_ELSE(13, ":", true),
    TERNARY_WAS_FALSE(13, "?!", true), // NOT A REAL TOKEN -- REPLACES `TERNARY_THEN` IF CONDITION IS FALSE
    COMMA(14, ","),
    EXPRESSION_BEGIN(15, "${"),
    EXPRESSION_BEGIN_LEGACY(15, "<%"),
    EXPRESSION_END(15, "}"),
    EXPRESSION_END_LEGACY(15, "%>"),
    WHITESPACE(16, null); // Not a real token

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
            EXPRESSION_BEGIN, EXPRESSION_BEGIN_LEGACY, EXPRESSION_END, EXPRESSION_END_LEGACY,
            OP_TIMES, OP_DIVIDE, OP_MODULO, OP_MINUS, OP_PLUS,
            NAMESPACE_ACCESSOR,
            CMP_LT, CMP_LTE, CMP_GT, CMP_GTE, CMP_EQUALS, CMP_NEQUALS,
            LOG_AND, LOG_OR,
            TERNARY_THEN, TERNARY_ELSE,
            BITSHIFT_LEFT, BITSHIFT_RIGHT, BIT_AND, BIT_OR, BIT_XOR,
            PAREN_LEFT, PAREN_RIGHT, COMMA
    };

    public static TokenType[] GetOperatorTokenTypes() {
        return operatorTokens;
    }

    public boolean isExpressionBeginOrEnd() {
        return isExpressionBegin() || isExpressionEnd();
    }

    public boolean isLegacyExpressionBeginOrEnd() {
        return this == EXPRESSION_BEGIN_LEGACY || this == EXPRESSION_END_LEGACY;
    }

    public boolean isExpressionBegin() {
        return this == EXPRESSION_BEGIN || this == EXPRESSION_BEGIN_LEGACY;
    }

    public boolean isExpressionEnd() {
        return this == EXPRESSION_END || this == EXPRESSION_END_LEGACY;
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

    public boolean operatorSupportsStringArguments() {
        return this == CMP_EQUALS || this == CMP_NEQUALS || this == OP_PLUS;
    }

    public boolean isBitShiftOperator() {
        return this == BITSHIFT_LEFT || this == BITSHIFT_RIGHT;
    }

    public boolean isLogicalBitwiseOperator() {
        return this == BIT_AND || this == BIT_OR || this == BIT_XOR;
    }

    public boolean isBitwiseOperator() {
        return isLogicalBitwiseOperator() || isBitShiftOperator();
    }

    public boolean isTernaryOperator() {
        return this == TERNARY_THEN || this == TERNARY_ELSE;
    }

    public boolean isBooleanOperator() {
        return isLogicalOperator() || isComparatorOperator();
    }

    public boolean isNamespaceAccessor() {
        return this == NAMESPACE_ACCESSOR;
    }

    public boolean isOperator() {
        return isArithmetic()
                || isMultDiv()
                || isLogicalOperator()
                || isComparatorOperator()
                || isTernaryOperator()
                || isBitwiseOperator()
                || isNamespaceAccessor()
                || this == COMMA;
    }

    public boolean isParenthesis() {
        return this == PAREN_LEFT || this == PAREN_RIGHT;
    }

    public boolean isValue() {
        return isVariable() || isConstant() || this == LAMBDA || this == NAMESPACE || this == GENERATOR;
    }

    public boolean isFunction() {
        return this == FUNCTION;
    }

    public boolean isVariable() {
        return this == VARIABLE;
    }

    public boolean isConstant() {
        return this == INTEGER || this == DOUBLE || this == STRING || this == NAMESPACE;
    }

    public boolean isIdentifier() {
        return isValue() || isFunction();
    }
}