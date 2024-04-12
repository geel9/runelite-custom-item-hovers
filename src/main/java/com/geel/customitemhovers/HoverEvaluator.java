package com.geel.customitemhovers;

import com.geel.customitemhovers.expressions.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/***
 * Evaluates a hover text, replacing variable names with their values, and replacing function calls with their results.
 */
@Singleton
public class HoverEvaluator {

    @Inject
    private ExpressionEvaluator expressionEvaluator;

    /**
     * Evaluates a hover text for a given item.
     *
     * Evaluates expressions, returning a string ready to render into a hover box.
     *
     * @return A hover string, properly evaluated, ready to render into a hover box.
     */
    public String Evaluate(ParsedHover hover, ItemHoverContext context) {
        // No expressions? Nothing to evaluate
        if(hover.getExpressions().length == 0) {
            return hover.getHoverText();
        }

        // Evaluate expressions (in the form ${expression} or <%expression%> (backwards-compat))
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
        StringBuilder ret = new StringBuilder();

        // For each expression in the hover text, copy the static text that precedes it,
        // then evaluate the expression and copy the result
        int pos = 0;
        for(int i = 0; i < hover.getExpressionLocations().length; i++) {
            int nextPos = hover.getExpressionLocations()[i];
            Token[] expression = hover.getExpressions()[i];

            // Append all text preceding this expression
            ret.append(hover.getHoverText(), pos, nextPos);

            // Insert the expression
            ret.append(formatter.format(expressionEvaluator.Evaluate(expression, context)));

            // Update pos to nextPos
            pos = nextPos;
        }

        // After the last expression, there might be some static text left to copy
        if(pos < hover.getHoverText().length()) {
            ret.append(hover.getHoverText(), pos, hover.getHoverText().length());
        }

        return ret.toString();
    }
}
