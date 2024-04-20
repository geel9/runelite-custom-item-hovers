package com.geel.customitemhovers.expressions;

import com.geel.customitemhovers.expressions.models.CompiledExpression;
import com.geel.customitemhovers.expressions.models.Token;

/**
 * Evaluates a CompiledExpression at runtime.
 */
public class RuntimeEvaluator {
    /**
     * Evaluates a Reverse Polish Notation queue (output from `ExpressionCompiler`), returning the result
     */
    public static Token Evaluate(CompiledExpression expression, ExecutionContext context) {
        Token[] evaluationResult = EvaluationUtil.evaluateExpression(expression, context, false);

        // Stack should have one entry: the result
        if (evaluationResult.length != 1) {
            throw new IllegalArgumentException("Stack should only have a single entry at the end");
        }

        // Single entry should be a constant integer, double, or string value
        Token result = evaluationResult[0];
        if (!result.getType().isConstant()) {
            throw new IllegalArgumentException("Invalid token type after RPN evaluation");
        }

        // Return result
        return result;
    }
}
