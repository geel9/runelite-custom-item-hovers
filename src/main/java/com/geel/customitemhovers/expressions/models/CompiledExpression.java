package com.geel.customitemhovers.expressions.models;

/**
 * A "compiled" expression, after tokenization and parsing.
 *
 * TODO: Figure out a way to collapse constexpr expressions, eg:
 *      "(3 + 4) * 2" should collapse to a single constant token "24"
 *      "sqrt(3 + 4 + 2) * a" should collapse to "3.0 * a"
 */
public class CompiledExpression extends BaseExpression {
    public CompiledExpression(Token[] tokens, String expressionText, boolean isLegacy) {
        super(tokens, expressionText, isLegacy);
    }

    public CompiledExpression(BaseExpression expression) {
        super(expression.getTokens(), expression.getExpressionText(), expression.isLegacy());
    }
}
