package com.geel.customitemhovers;

import com.geel.customitemhovers.expressions.*;
import com.geel.customitemhovers.expressions.models.*;
import com.geel.customitemhovers.models.hovers.ParsedHover;
import java.text.NumberFormat;
import java.util.Locale;

/***
 * Evaluates a hover text, replacing any expressions with their evaluated results.
 */
public class HoverEvaluator {
    /**
     * Evaluates a hover text for a given item.
     *
     * Evaluates expressions, returning a string ready to render into a hover box.
     *
     * @return A hover string, properly evaluated, ready to render into a hover box.
     */
    public static String Evaluate(ParsedHover hover, ExecutionContext context) {
        // If the ParsedHover has a condition, evaluate it -- if it returns a falsey value, don't render!
        if(hover.getCondition() != null) {
            Token conditionResult = EvaluationUtil.EvaluateAtRuntime(hover.getCondition(), context);
            if(conditionResult.getNumericValue().intValue() == 0) {
                return null;
            }
        }

        // No expressions? Nothing to evaluate
        if(hover.getExpressions().length == 0) {
            return hover.getHoverText();
        }

        return evaluateExpressions(hover, context);
    }

    private static String evaluateExpressions(ParsedHover hover, ExecutionContext context) {
        // Evaluate expressions (in the form ${expression} or <%expression%> (backwards-compat))
        StringBuilder ret = new StringBuilder();

        // For each expression in the hover text, copy the static text that precedes it,
        // then evaluate the expression and copy the result
        int pos = 0;
        for(int i = 0; i < hover.getExpressionLocations().length; i++) {
            int nextPos = hover.getExpressionLocations()[i];
            CompiledExpression expression = hover.getExpressions()[i];

            // Append all text preceding this expression
            ret.append(hover.getHoverText(), pos, nextPos);

            // Insert the expression
            ret.append(evaluateExpression(expression, context));

            // Update pos to nextPos
            pos = nextPos;
        }

        // After the last expression, there might be some static text left to copy
        if(pos < hover.getHoverText().length()) {
            ret.append(hover.getHoverText(), pos, hover.getHoverText().length());
        }

        return ret.toString();
    }

    private static String evaluateExpression(CompiledExpression expression, ExecutionContext context) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());

        try {
            Token result = EvaluationUtil.EvaluateAtRuntime(expression, context);

            if(result.getType() == TokenType.STRING) {
                return result.getValue();
            }

            return formatter.format(result.getNumericValue());
        } catch (IllegalArgumentException e) {
            return "${" + expression.getExpressionText() + "}";
        }
    }
}
