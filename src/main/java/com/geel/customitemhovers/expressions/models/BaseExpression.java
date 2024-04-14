package com.geel.customitemhovers.expressions.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class BaseExpression {
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

    /**
     * Whether the expression is "legacy" (begins with <% and ends with %>)
     */
    @Getter
    private boolean isLegacy = false;
}
