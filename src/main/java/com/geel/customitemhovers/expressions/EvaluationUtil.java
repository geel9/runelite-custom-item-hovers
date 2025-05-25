package com.geel.customitemhovers.expressions;

import com.geel.customitemhovers.expressions.models.CompiledExpression;
import com.geel.customitemhovers.expressions.models.Token;

/**
 * Helpers for simplifying the evaluation of expressions
 */
public class EvaluationUtil {
    /**
     * Evaluates a Reverse Polish Notation queue (output from `ExpressionCompiler`), returning the result
     */
    public static Token EvaluateAtRuntime(CompiledExpression expression, ExecutionContext context) {
        context = new ExecutionContext(context, false);
        Token[] evaluationResult = Evaluator.EvaluateExpression(expression, context);

        // Stack should have one entry: the result
        if (evaluationResult.length != 1) {
            throw new IllegalArgumentException("Stack should only have a single entry at the end");
        }

        // Single entry should be a value type; otherwise, evaluation failed :(
        Token result = evaluationResult[0];
        if (!result.getType().isValue()) {
            throw new IllegalArgumentException("Invalid token type after RPN evaluation");
        }

        // Return result
        return result;
    }

    /**
     * Statically evaluates a CompiledExpression, returning its simplified form.
     *
     * If the expression is fully statically evaluatable, the resultant `CompiledExpression` will contain only
     * a single Token, which is the result of evaluation.
     *
     *
     *
     * This will attempt to execute as much of the expression as possible, replacing constant operations (eg "3 + 4") with
     * their results.
     *
     * The end result will, ideally, be the simplest form of an expression possible, with all operations on constant values pre-resolved.
     *
     * For example, take the following expression: (3 + (4 - sqrt(sqrt(81))) - pow(2, 0) + (2 * a)
     *
     * This expression references a variable -- `a` -- which we will only be able to resolve at runtime, when we're hovering over an item.
     * However, it also contains many static math operations, including some functions -- `pow` and `sqrt`.
     *
     * Both functions called (`pow` and `sqrt`) are `constexpr` (meaning they can be evaluated without any game state),
     * and the arguments being fed into them are const themselves.
     *
     * sqrt(sqrt(81)) should simplify to sqrt(9) -> 3
     * pow(2,0) should simplify to 1
     *
     * Therefore, the entire above expression can be statically evaluated to:
     * (3 + (4 - 3)) - 1 + (2 * a) -> (4) - 1 + (2 * a) -> 3 + (2 * a)
     *
     * Which, in RPN form, would be: 2 a * 3 +
     *
     * That's a mere 5 tokens that we have to execute at runtime, instead of however many were in the original expression.
     */
    public static CompiledExpression EvaluateStatically(CompiledExpression expression, ExecutionContext context) {
        context = new ExecutionContext(context, true);
        Token[] evaluationResult = Evaluator.EvaluateExpression(expression, context);

        // Stack should not be empty
        if (evaluationResult.length == 0) {
            throw new IllegalArgumentException("Stack empty??");
        }

        // Return result
        return new CompiledExpression(evaluationResult, expression.getExpressionText(), expression.isLegacy());
    }
}
