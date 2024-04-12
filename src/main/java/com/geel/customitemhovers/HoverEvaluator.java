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
    // All legacy variables -- from before tables
    private static final String[] LEGACY_HOVER_VARIABLES = {"ID", "QTY", "VALUE", "HIGH_ALCH"};

    //Regex for all currently supported functions.
    private final Pattern funcFinder = Pattern.compile("<%(qtymult|qtydivide|qtydivideceil)(\\((\\d+)\\))?%>");

    //Regex for table references
    private final Pattern expressionFinder = Pattern.compile("\\$\\{([a-zA-Z0-9_/*+ ().\\-]*)}");

    //Regex for all supported legacy variables. Generated at runtime as a singleton.
    private Pattern legacyVariableFinder = null;

    @Inject
    private ExpressionEvaluator expressionEvaluator;

    /**
     * Computes (if necessary) and returns variableFinder
     */
    private Pattern getLegacyVariableRegex() {
        if (legacyVariableFinder != null)
            return legacyVariableFinder;

        //Concatenate LEGACY_HOVER_VARIABLES into a "|"-delimited string
        StringBuilder variableNames = new StringBuilder();
        for (String s : LEGACY_HOVER_VARIABLES) {
            variableNames.append(s);
            variableNames.append("|");
        }

        //Delete final dangling | character
        variableNames.deleteCharAt(variableNames.length() - 1);

        //Wrap variable names with regex
        //We should end up with a Regex like:
        //  <%(VARIABLE_1|VARIABLE_2|...|VARIABLE_N)%>
        variableNames.insert(0, "<%(");
        variableNames.append(")%>");

        legacyVariableFinder = Pattern.compile(variableNames.toString());
        return legacyVariableFinder;
    }

    /**
     * Evaluates a hover text for a given item.
     * <p>
     * Evaluates functions and variables, returning a string ready to render into a hover box.
     *
     * @return A hover string, properly evaluated, ready to render into a hover box.
     */
    public String Evaluate(String text, ItemHoverContext context) {
        String ret = text;

        //Handle legacy variables (in the form <%VARIABLE_NAME%>)
        ret = EvaluateVariables(ret, context);

        // Evaluate table references (in the form ${variable_name}
        ret = EvaluateExpressions(ret, context);

        //Handle functions (in the form <%function_name(arg)%>
        ret = EvaluateFunctions(ret, context);

        return ret;
    }

    /**
     * Replaces all variables in a given text with their value.
     *
     * @return The text with all variables replaced with their value.
     */
    private String EvaluateVariables(String text, ItemHoverContext context) {
        String ret = text;

        Matcher varMatcher = getLegacyVariableRegex().matcher(text);

        while (varMatcher.find()) {
            MatchResult result = varMatcher.toMatchResult();
            String matchText = result.group();
            String varName = result.group(1).toLowerCase(Locale.ROOT);

            // The Base Table is filled with variables that intentionally match the legacy variable names
            // Therefore, we can just search the base table
            String replaceWith = context.getBaseTable().getOrDefault(varName, null);
            if (replaceWith == null)
                continue;

            ret = ret.replace(matchText, replaceWith);
        }

        return ret;
    }

    /**
     * Replaces all expressions in a given text with their value.
     *
     * @return The text with all variables replaced with their value.
     */
    private String EvaluateExpressions(String text, ItemHoverContext context) {
        String ret = text;

        Matcher tableMatcher = expressionFinder.matcher(text);
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());

        while (tableMatcher.find()) {
            MatchResult result = tableMatcher.toMatchResult();
            String matchText = result.group();
            String expression = result.group(1);

            String replaceWith;
            try {
                replaceWith = formatter.format(expressionEvaluator.Evaluate(expression, context));
            } catch (Exception e) {
                replaceWith = e.getMessage(); // hehe
            }

            if(replaceWith == null) {
                continue;
            }

            ret = ret.replace(matchText, replaceWith);
        }

        return ret;
    }

    /**
     * Evaluates all functions in a given text, replacing them with their value.
     *
     * @return The text with all function calls replaced with their resultant value.
     */
    private String EvaluateFunctions(String text, ItemHoverContext context) {
        String ret = text;

        Matcher funcMatcher = funcFinder.matcher(text);

        while (funcMatcher.find()) {
            MatchResult result = funcMatcher.toMatchResult();
            String matchText = result.group();
            String funcName = result.group(1);
            String args = result.group(3);
            boolean hasArgs = args != null;

            int itemQty = context.getItem().getQuantity();
            int argument = 1;
            if (hasArgs) {
                argument = Integer.parseInt(args);
            }

            String replaceWith = matchText;

            NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());

            // TODO: This all seems pretty inelegant. But this is also a hobby project, and it works.
            switch (funcName) {
                //qtymult(x) returns (stack_size * x)
                case "qtymult":
                    replaceWith = formatter.format((long) itemQty * argument);
                    break;
                // qtydivideceil(x) returns ceil(stack_size / x)
                case "qtydivideceil":
                    replaceWith = formatter.format(Math.ceil((double) itemQty / (double) argument));
                    break;
                // qtydivideceil(x) returns (stack_size / x) as integer division, rounding towards zero.
                case "qtydivide":
                    replaceWith = formatter.format(itemQty / argument);
                    break;
                default:
                    break;
            }

            if (replaceWith == null || replaceWith.equals(matchText))
                continue;

            ret = ret.replace(matchText, replaceWith);
        }

        return ret;
    }
}
