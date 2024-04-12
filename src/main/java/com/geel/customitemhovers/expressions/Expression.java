package com.geel.customitemhovers.expressions;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A "compiled" expression, after tokenization and parsing.
 *
 * TODO: Figure out a way to collapse constexpr expressions, eg:
 *      "(3 + 4) * 2" should collapse to a single constant token "24"
 *      "sqrt(3 + 4 + 2) * a" should collapse to "3.0 * a"
 */
@AllArgsConstructor
public class Expression {
    /**
     * The Tokens that make up this expression, in Reverse Polish Notation
     */
    @Getter
    private Token[] tokens;

    /**
     * The original raw text of the expression
     */
    @Getter
    private String expressionText;
}
