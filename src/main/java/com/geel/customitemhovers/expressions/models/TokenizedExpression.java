package com.geel.customitemhovers.expressions.models;

/**
 * An expression, after being tokenized, but before parsing.
 */
public class TokenizedExpression extends BaseExpression {
    public TokenizedExpression(Token[] tokens, String expressionText, boolean isLegacy) {
        super(tokens, expressionText, isLegacy);
    }

    public TokenizedExpression(BaseExpression expression) {
        super(expression.getTokens(), expression.getExpressionText(), expression.isLegacy());
    }
}
