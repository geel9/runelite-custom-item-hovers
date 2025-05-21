package com.geel.customitemhovers;

import com.geel.customitemhovers.expressions.ExecutionContext;
import com.geel.customitemhovers.expressions.functions.Function;
import com.geel.customitemhovers.expressions.functions.LambdaFixedArgsFunction;
import com.geel.customitemhovers.expressions.functions.LambdaVariadicFunction;
import com.geel.customitemhovers.expressions.models.ArgumentType;
import com.geel.customitemhovers.expressions.models.Token;
import com.geel.customitemhovers.expressions.models.TokenType;
import com.geel.customitemhovers.expressions.providers.IFunctionProvider;
import lombok.Getter;
import net.runelite.api.*;
import net.runelite.client.game.ItemManager;
import net.runelite.client.util.ColorUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.util.*;

/**
 * Provides functions to the expression engine.
 */
@Singleton
public class CustomItemHoversFunctionProvider implements IFunctionProvider {
    // A map(functionName -> map(numArguments -> listOfFunctions))
    @Getter
    private HashMap<String, HashMap<Integer, ArrayList<Function>>> functions;

    public Client client;

    public ItemManager itemManager;

    @Inject
    public CustomItemHoversFunctionProvider(Client client, ItemManager itemManager) {
        this.client = client;
        this.itemManager = itemManager;

        functions = new HashMap<>();
        prepareFunctions();
    }

    public Function findFunction(String name, Token[] arguments) {
        int numArgs = arguments.length;

        // Find the function by name. The result will be a map(numArgs -> list_of_functions)
        HashMap<Integer, ArrayList<Function>> numArgsMap = functions.getOrDefault(name, null);

        // If no functions found, obviously return null
        if (numArgsMap == null) {
            return null;
        }

        // Look for it in the fixed functions, then try variadic functions
        ArrayList<Function> fixedFunctions = numArgsMap.getOrDefault(numArgs, null);
        ArrayList<Function> variadicFunctions = numArgsMap.getOrDefault(-1, null);

        Function fixedFunction = searchFuncs(fixedFunctions, arguments);
        if (fixedFunction != null)
            return fixedFunction;

        return searchFuncs(variadicFunctions, arguments);
    }

    private Function searchFuncs(ArrayList<Function> functions, Token[] arguments) {
        if (functions == null)
            return null;

        for (Function func : functions) {
            if (func.argumentsMatch(arguments)) {
                return func;
            }
        }

        return null;
    }

    public void addFunction(Function f) {
        String name = f.getName();

        // Ensure that the overarching map has an entry for the function name
        if (!functions.containsKey(name)) {
            functions.put(name, new HashMap<>());
        }

        // Get the entry for the function name, and ensure the sub-map has an entry for the number of arguments
        HashMap<Integer, ArrayList<Function>> argumentsMap = functions.get(name);
        int argsHashCode = f.getArgumentsHashCode();
        if (!argumentsMap.containsKey(argsHashCode)) {
            argumentsMap.put(argsHashCode, new ArrayList<>(1));
        }

        // Get the entry for the number of arguments from the sub-map, and insert the function into the list
        ArrayList<Function> funcList = argumentsMap.get(argsHashCode);
        if (funcList.contains(f)) {
            throw new IllegalArgumentException("Function already exists");
        }

        funcList.add(f);
    }

    private void prepareFunctions() {
        // {inv}_contains() and {inv}_qty()
        prepareInventoryFunctions();

        // floor(), ceil(), pow(), max(), min(), abs()
        prepareMathFunctions();

        // len(), contains(), red(), green(), blue(), yellow(), rgb()
        prepareStringFunctions();

        // varbit(), varplayer(), varcint(), varcstr()
        prepareVarBitFunctions();

        // qtymult(), qtydivide(), qtydivideceil()
        prepareBackwardsCompatFunctions();
    }

    /**
     * Prepares inventory-related functions.
     *
     * EG, inv_contains(), bank_contains(), inv_qty(), etc
     */
    private void prepareInventoryFunctions() {
        inventoryFunction("inv", InventoryID.INVENTORY);
        inventoryFunction("bank", InventoryID.BANK);
        inventoryFunction("equipment", InventoryID.EQUIPMENT);
    }

    /**
     * Creates varbit-related functions
     */
    private void prepareVarBitFunctions() {
        // The varbit(varbitId) function returns the integer value of a varbit
        addFunction(new LambdaFixedArgsFunction("varbit", TokenType.INTEGER, new ArgumentType[]{ArgumentType.INTEGER}, (args, context) -> {
            if(client == null) return Token.fromFalse();
            return Token.fromInt(client.getVarbitValue(args[0].getNumericValue().intValue()));
        }));

        // The varplayer(varPlayerId) function returns the integer value of a varplayer
        addFunction(new LambdaFixedArgsFunction("varplayer", TokenType.INTEGER, new ArgumentType[]{ArgumentType.INTEGER}, (args, context) -> {
            if(client == null) return Token.fromFalse();
            return Token.fromInt(client.getVarpValue(args[0].getNumericValue().intValue()));
        }));

        // The varcint(varCIntId) function returns the integer value of a varcint
        addFunction(new LambdaFixedArgsFunction("varcint", TokenType.INTEGER, new ArgumentType[]{ArgumentType.INTEGER}, (args, context) -> {
            if(client == null) return Token.fromFalse();
            return Token.fromInt(client.getVarcIntValue(args[0].getNumericValue().intValue()));
        }));

        // The varcstr(varCStrId) function returns the string value of a varcstr
        addFunction(new LambdaFixedArgsFunction("varcstr", TokenType.STRING, new ArgumentType[]{ArgumentType.INTEGER}, (args, context) -> {
            if(client == null) return Token.fromFalse();
            return Token.fromString(client.getVarcStrValue(args[0].getNumericValue().intValue()));
        }));
    }

    /**
     * Creates string- and color-related functions
     */
    private void prepareStringFunctions() {
        addFunction(new LambdaFixedArgsFunction("len", TokenType.INTEGER, new ArgumentType[]{ArgumentType.STRING}, true, (args, context) -> {
            return Token.fromInt(args[0].getValue().length());
        }));

        addFunction(new LambdaFixedArgsFunction("contains", TokenType.INTEGER, new ArgumentType[]{ArgumentType.STRING, ArgumentType.STRING}, true, (args, context) -> {
            return Token.fromInt(args[0].getValue().contains(args[1].getValue()) ? 1 : 0);
        }));

        // Colors
        // Wrapping functions -- red(), green(), blue(), yellow()
        simpleColorStringFunc("red", Color.RED);
        simpleColorStringFunc("green", Color.GREEN);
        simpleColorStringFunc("blue", Color.BLUE);
        simpleColorStringFunc("yellow", Color.YELLOW);

        // Tag *start* functions -- redStart(), greenStart(), blueStart(), yellowStart()
        simpleColorStringStartFunc("red", Color.RED);
        simpleColorStringStartFunc("green", Color.GREEN);
        simpleColorStringStartFunc("blue", Color.BLUE);
        simpleColorStringStartFunc("yellow", Color.YELLOW);

        // colorEnd()
        addFunction(new LambdaFixedArgsFunction("colorEnd", TokenType.STRING,
                new ArgumentType[]{},
                true,
                (args, context) -> {
                    return Token.fromString("</col>");
                }));

        // rgb()
        addFunction(new LambdaFixedArgsFunction("rgb", TokenType.STRING,
                new ArgumentType[]{ArgumentType.INTEGER, ArgumentType.INTEGER, ArgumentType.INTEGER, ArgumentType.STRING},
                true,
                (args, context) -> {
                    int r = args[0].getNumericValue().intValue() % 256;
                    int g = args[1].getNumericValue().intValue() % 256;
                    int b = args[2].getNumericValue().intValue() % 256;

                    Color color = new Color(r, g, b);
                    return Token.fromString(ColorUtil.wrapWithColorTag(args[3].getValue(), color));
                }));

        // rgbStart()
        addFunction(new LambdaFixedArgsFunction("rgbStart", TokenType.STRING,
                new ArgumentType[]{ArgumentType.INTEGER, ArgumentType.INTEGER, ArgumentType.INTEGER},
                true,
                (args, context) -> {
                    int r = args[0].getNumericValue().intValue() % 256;
                    int g = args[1].getNumericValue().intValue() % 256;
                    int b = args[2].getNumericValue().intValue() % 256;

                    Color color = new Color(r, g, b);
                    return Token.fromString(ColorUtil.colorTag(color));
                }));
    }

    /**
     * Creates the floor(), ceil(), pow(), max(), min() functions
     */
    private void prepareMathFunctions() {
        // abs(x) takes a number and returns the absolute value
        addFunction(new LambdaFixedArgsFunction("abs", TokenType.DOUBLE, new ArgumentType[]{ArgumentType.DOUBLE}, true, (args, context) -> {
            return Token.fromDouble(Math.abs(args[0].getNumericValue().doubleValue()));
        }));
        addFunction(new LambdaFixedArgsFunction("abs", TokenType.INTEGER, new ArgumentType[]{ArgumentType.INTEGER}, true, (args, context) -> {
            return Token.fromInt(Math.abs(args[0].getNumericValue().intValue()));
        }));

        // floor(x) takes a double and returns an integer
        addFunction(new LambdaFixedArgsFunction("floor", TokenType.INTEGER, new ArgumentType[]{ArgumentType.DOUBLE},true, (args, context) -> {
            return Token.fromInt((int) Math.floor(args[0].getNumericValue().doubleValue()));
        }));

        // ceil(x) takes a double and returns an integer
        addFunction(new LambdaFixedArgsFunction("ceil", TokenType.INTEGER, new ArgumentType[]{ArgumentType.DOUBLE},true, (args, context) -> {
            return Token.fromInt((int) Math.ceil(args[0].getNumericValue().doubleValue()));
        }));

        // pow(x) takes two numbers and returns the first to the power of the second as a double
        addFunction(new LambdaFixedArgsFunction("pow", TokenType.DOUBLE, new ArgumentType[]{ArgumentType.DOUBLE, ArgumentType.DOUBLE}, true, (args, context) -> {
            return Token.fromDouble(Math.pow(args[0].getNumericValue().doubleValue(), args[1].getNumericValue().doubleValue()));
        }));

        // max(...) takes in any amount of numbers and returns the largest one
        addFunction(new LambdaVariadicFunction("max", TokenType.DOUBLE, ArgumentType.DOUBLE, new ArgumentType[]{ArgumentType.DOUBLE}, true, (args, context) -> {
            double max = Double.MIN_VALUE;

            for(Token t : args) {
                double value = t.getNumericValue().doubleValue();
                if(value > max) {
                    max = value;
                }
            }

            return Token.fromDouble(max);
        }));

        // min(...) takes in any amount of numbers and returns the smallest one
        addFunction(new LambdaVariadicFunction("min", TokenType.DOUBLE, ArgumentType.DOUBLE, new ArgumentType[]{ArgumentType.DOUBLE}, true, (args, context) -> {
            double min = Double.MAX_VALUE;

            for(Token t : args) {
                double value = t.getNumericValue().doubleValue();
                if(value < min) {
                    min = value;
                }
            }

            return Token.fromDouble(min);
        }));
    }

    /**
     * Creates functions which exist for backwards compatibility with the previous (terrible) "expression" engine
     */
    private void prepareBackwardsCompatFunctions() {
        // THE FOLLOWING FUNCTIONS ARE ONLY PRESENT FOR BACKWARDS COMPATIBILITY WITH THE OLD SHITTY SYSTEM
        // qtymult(x) takes an integer and returns an integer
        addFunction(new LambdaFixedArgsFunction("qtymult", TokenType.INTEGER, new ArgumentType[]{ArgumentType.INTEGER}, (args, context) -> {
            String numInStackStr = context.resolveVariable("qty");
            if(numInStackStr == null) return Token.fromInt(0);

            return Token.fromInt(Integer.parseInt(numInStackStr) * args[0].getNumericValue().intValue());
        }));
        // qtydivide(x) takes an integer and returns an integer
        addFunction(new LambdaFixedArgsFunction("qtydivide", TokenType.INTEGER, new ArgumentType[]{ArgumentType.INTEGER}, (args, context) -> {
            String numInStackStr = context.resolveVariable("qty");
            if(numInStackStr == null) return Token.fromInt(0);

            return Token.fromInt(Integer.parseInt(numInStackStr) / args[0].getNumericValue().intValue());
        }));
        // qtydivideceil(x) takes an integer and returns an integer
        addFunction(new LambdaFixedArgsFunction("qtydivideceil", TokenType.INTEGER, new ArgumentType[]{ArgumentType.INTEGER}, (args, context) -> {
            String numInStackStr = context.resolveVariable("qty");
            if(numInStackStr == null) return Token.fromInt(0);

            return Token.fromInt((int) Math.ceil((double) Integer.parseInt(numInStackStr) / (double) args[0].getNumericValue().intValue()));
        }));
    }

    /**
     * Implements the logic for the `{inv}_qty()` functions
     */
    private Token containerQty(Token[] args, ExecutionContext context, InventoryID inventoryID, boolean shortCircuitOnFind) {
        if (client == null || client.getLocalPlayer() == null)
            return Token.fromInt(0);

        ItemContainer container = client.getItemContainer(inventoryID);
        if (container == null) {
            return Token.fromInt(0);
        }

        // Loop through the arguments, constructing a list of item IDs
        Set<Integer> itemIDs = new HashSet<>(args.length);
        for(Token arg : args) {
            if (arg.getType() == TokenType.STRING) {
                for(int itemId : ItemNameMap.GetItemIDs(arg.getValue())) {
                    itemIDs.add(itemId);
                }
            } else {
                int itemID = arg.getNumericValue().intValue();
                if (itemManager != null) itemID = itemManager.canonicalize(itemID);

                itemIDs.add(itemID);
            }
        }

        // No item IDs are relevant -- so zero
        if (itemIDs.size() == 0)
            return Token.fromInt(0);

        // Find the sum of quantities of items in the given inventory which are represented in `itemIDs`
        int count = 0;
        for (Item item : container.getItems()) {
            int canonicalId = itemManager == null ? item.getId() : itemManager.canonicalize(item.getId());
            if (itemIDs.contains(canonicalId)) {
                count += item.getQuantity();

                // If we should short-circuit when we find the first match, then... do that.
                if(shortCircuitOnFind) {
                    break;
                }
            }
        }

        return Token.fromInt(count);
    }

    /**
     * Implements the logic for the `{inv}_contains()` functions
     */
    private Token containerContainsAnyItem(Token[] args, ExecutionContext context, InventoryID inventoryID) {
        Token qty = containerQty(args, context, inventoryID, true);
        return Token.fromBool(qty.getNumericValue().intValue() > 0);
    }

    /**
     * Creates the `{inventory}_qty()` and `{inventory}_contains()` functions for a given inventory
     */
    private void inventoryFunction(String prefix, InventoryID inventoryID) {
        String containsFuncName = prefix + "_contains";
        String qtyFuncName = prefix + "_qty";

        // {prefix}_contains(idOrName, ...) takes variable item IDs/names and returns 1 if any of said items are in the specified inventory
        addFunction(new LambdaVariadicFunction(containsFuncName, TokenType.INTEGER, ArgumentType.ANY, null, (args, context) -> {
            return containerContainsAnyItem(args, context, inventoryID);
        }));

        // {prefix}_qty(idOrName, ...) takes variable item IDs/names and returns the number of items with any of said IDs in the given inventory
        addFunction(new LambdaVariadicFunction(qtyFuncName, TokenType.INTEGER, ArgumentType.ANY, null, (args, context) -> {
            return containerQty(args, context, inventoryID, false);
        }));
    }

    private void simpleColorStringFunc(String colorName, Color color) {
        addFunction(new LambdaFixedArgsFunction(colorName, TokenType.STRING, new ArgumentType[]{ArgumentType.ANY}, true, (args, context) -> {
            return Token.fromString(ColorUtil.wrapWithColorTag(args[0].getValue(), color));
        }));
    }

    private void simpleColorStringStartFunc(String colorName, Color color) {
        addFunction(new LambdaFixedArgsFunction(colorName + "Start", TokenType.STRING, new ArgumentType[]{}, true, (args, context) -> {
            return Token.fromString(ColorUtil.colorTag(color));
        }));
    }
}
