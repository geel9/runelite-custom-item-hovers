package com.geel.customitemhovers;

import net.runelite.api.Item;
import net.runelite.api.ItemComposition;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/***
 * Evaluates a hover text, replacing variable names with their values, and replacing function calls with their results.
 */
public class HoverEvaluator {
    //All supported functions. Unused presently (regex is manually typed)
    private static final String[] HOVER_FUNCTIONS = {"qtymult"};

    //All supported variables
    private static final String[] HOVER_VARIABLES = {"ID", "QTY", "VALUE", "HIGH_ALCH"};

    //Regex for all currently supported functions.
    private static final Pattern funcFinder = Pattern.compile("<%(qtymult)(\\((\\d+)\\))?%>");

    //Regex for all currently supported variables. Generated at runtime as a singleton.
    private static Pattern variableFinder = null;

    /**
     * Computes (if necessary) and returns variableFinder
     */
    private static Pattern getVariableRegex() {
        if (variableFinder != null)
            return variableFinder;

        //Concatenate HOVER_VARIABLES into a "|"-delimited string
        StringBuilder variableNames = new StringBuilder();
        for (String s : HOVER_VARIABLES) {
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

        variableFinder = Pattern.compile(variableNames.toString());
        return variableFinder;
    }

    /**
     * Evaluates a hover text for a given item.
     *
     * Evaluates functions and variables, returning a string ready to render into a hover box.
     *
     * @return A hover string, properly evaluated, ready to render into a hover box.
     */
    public static String Evaluate(String text, Item item, ItemComposition comp) {
        String ret = text;

        //Handle variables
        ret = EvaluateVariables(ret, item, comp);

        //Handle functions
        ret = EvaluateFunctions(ret, item, comp);

        return ret;
    }

    /**
     * Replaces all variables in a given text with their value.
     *
     * @return The text with all variables replaced with their value.
     */
    private static String EvaluateVariables(String text, Item item, ItemComposition comp) {
        String ret = text;

        Matcher varMatcher = getVariableRegex().matcher(text);

        while (varMatcher.find()) {
            MatchResult result = varMatcher.toMatchResult();
            String matchText = result.group();
            String varName = result.group(1);

            String replaceWith = matchText;

            switch (varName) {
                //<%ID%> is just the numeric ID of the item
                case "ID":
                    replaceWith = String.valueOf(item.getId());
                    break;
                //<%QTY%> is the number of items in the stack
                case "QTY":
                    replaceWith = String.valueOf(item.getQuantity());
                    break;
                //<%VALUE%> is the clientside price of the item
                case "VALUE":
                    replaceWith = String.valueOf(comp.getPrice());
                    break;
                //<%HIGH_ALCH%> is the high-alch price of the item
                case "HIGH_ALCH":
                    replaceWith = String.valueOf(comp.getHaPrice());
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

    /**
     * Evaluates all functions in a given text, replacing them with their value.
     *
     * @return The text with all function calls replaced with their resultant value.
     */
    private static String EvaluateFunctions(String text, Item item, ItemComposition comp) {
        String ret = text;

        Matcher funcMatcher = funcFinder.matcher(text);

        while (funcMatcher.find()) {
            MatchResult result = funcMatcher.toMatchResult();
            String matchText = result.group();
            String funcName = result.group(1);
            String args = result.group(3);
            boolean hasArgs = args != null;

            String replaceWith = matchText;
            switch (funcName) {
                //<%qtymult(x)%> returns (stack_size * x)
                case "qtymult":
                    int qty = item.getQuantity();
                    int mult = 1;

                    if (hasArgs) {
                        mult = Integer.parseInt(args);
                    }

                    qty *= mult;

                    String qtyText = NumberFormat.getNumberInstance(Locale.getDefault()).format(qty);
                    replaceWith = qtyText;
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
