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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

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
        HashMap<Integer, ArrayList<Function>> numArgsMap = functions.getOrDefault(name, null);

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
        if (!functions.containsKey(name)) {
            functions.put(name, new HashMap<>());
        }

        HashMap<Integer, ArrayList<Function>> argumentsMap = functions.get(name);
        int argsHashCode = f.getArgumentsHashCode();
        if (!argumentsMap.containsKey(argsHashCode)) {
            argumentsMap.put(argsHashCode, new ArrayList<>(1));
        }

        ArrayList<Function> funcList = argumentsMap.get(argsHashCode);
        if (funcList.contains(f)) {
            throw new IllegalArgumentException("Function already exists");
        }

        funcList.add(f);
    }

    private void prepareFunctions() {
        prepareInventoryFunctions();
        prepareMathFunctions();
        prepareStringFunctions();
        prepareVarBitFunctions();

        prepareBackwardsCompatFunctions();
    }

    private void prepareInventoryFunctions() {
        inventoryFunction("inv", InventoryID.INVENTORY);
        inventoryFunction("bank", InventoryID.BANK);
        inventoryFunction("equipment", InventoryID.EQUIPMENT);
    }

    private void prepareVarBitFunctions() {
        addFunction(new LambdaFixedArgsFunction("varbit", TokenType.INTEGER, new ArgumentType[]{ArgumentType.INTEGER}, (args, context) -> {
            if(client == null) return Token.fromFalse();
            return Token.fromInt(client.getVarbitValue(args[0].getNumericValue().intValue()));
        }));

        addFunction(new LambdaFixedArgsFunction("varplayer", TokenType.INTEGER, new ArgumentType[]{ArgumentType.INTEGER}, (args, context) -> {
            if(client == null) return Token.fromFalse();
            return Token.fromInt(client.getVarpValue(args[0].getNumericValue().intValue()));
        }));

        addFunction(new LambdaFixedArgsFunction("varcint", TokenType.INTEGER, new ArgumentType[]{ArgumentType.INTEGER}, (args, context) -> {
            if(client == null) return Token.fromFalse();
            return Token.fromInt(client.getVarcIntValue(args[0].getNumericValue().intValue()));
        }));

        addFunction(new LambdaFixedArgsFunction("varcstr", TokenType.STRING, new ArgumentType[]{ArgumentType.INTEGER}, (args, context) -> {
            if(client == null) return Token.fromFalse();
            return Token.fromString(client.getVarcStrValue(args[0].getNumericValue().intValue()));
        }));
    }

    private void prepareStringFunctions() {
        addFunction(new LambdaFixedArgsFunction("len", TokenType.INTEGER, new ArgumentType[]{ArgumentType.STRING}, true, (args, context) -> {
            return Token.fromInt(args[0].getValue().length());
        }));

        addFunction(new LambdaFixedArgsFunction("contains", TokenType.INTEGER, new ArgumentType[]{ArgumentType.STRING, ArgumentType.STRING}, true, (args, context) -> {
            return Token.fromInt(args[0].getValue().contains(args[1].getValue()) ? 1 : 0);
        }));

        // Colors
        simpleColorStringFunc("red", Color.RED);
        simpleColorStringFunc("green", Color.GREEN);
        simpleColorStringFunc("blue", Color.BLUE);
        simpleColorStringFunc("yellow", Color.YELLOW);

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
    }

    private void prepareMathFunctions() {
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

    private Token containerQty(Token[] args, ExecutionContext context, InventoryID inventoryID) {
        if (client == null || client.getLocalPlayer() == null)
            return Token.fromInt(0);

        ItemContainer container = client.getItemContainer(inventoryID);
        if (container == null) {
            return Token.fromInt(0);
        }

        Token arg = args[0];
        int[] itemIDs;

        if (arg.getType() == TokenType.STRING) {
            itemIDs = ItemNameMap.GetItemIDs(args[0].getValue());
        } else {
            int itemID = arg.getNumericValue().intValue();
            if (itemManager != null) itemID = itemManager.canonicalize(itemID);
            itemIDs = new int[]{itemID};
        }

        if (itemIDs.length == 0)
            return Token.fromInt(0);

        int count = 0;
        for (Item item : container.getItems()) {
            int canonicalId = itemManager == null ? item.getId() : itemManager.canonicalize(item.getId());
            if (Arrays.stream(itemIDs).anyMatch(id -> id == canonicalId)) {
                count += item.getQuantity();
            }
        }

        return Token.fromInt(count);
    }

    private Token containerContainsItem(Token[] args, ExecutionContext context, InventoryID inventoryID) {
        Token qty = containerQty(args, context, inventoryID);
        return Token.fromBool(qty.getNumericValue().intValue() > 0);
    }

    private void inventoryFunction(String prefix, InventoryID inventoryID) {
        String containsFuncName = prefix + "_contains";
        String qtyFuncName = prefix + "_qty";

        // {prefix}_contains(id) takes an item ID and returns 1 if the item is in the specified inventory
        addFunction(new LambdaFixedArgsFunction(containsFuncName, TokenType.INTEGER, new ArgumentType[]{ArgumentType.INTEGER}, (args, context) -> {
            return containerContainsItem(args, context, inventoryID);
        }));
        // {prefix}_contains(name) takes an item name and returns 1 if the item is in the specified inventory
        addFunction(new LambdaFixedArgsFunction(containsFuncName, TokenType.INTEGER, new ArgumentType[]{ArgumentType.STRING}, (args, context) -> {
            return containerContainsItem(args, context, inventoryID);
        }));

        // {prefix}_qty(id) takes an item ID and returns the number of items with that ID in the given inventory
        addFunction(new LambdaFixedArgsFunction(qtyFuncName, TokenType.INTEGER, new ArgumentType[]{ArgumentType.INTEGER}, (args, context) -> {
            return containerQty(args, context, inventoryID);
        }));
        // {prefix}_qty(name) takes an item ID and returns the number of items with that ID in the given inventory
        addFunction(new LambdaFixedArgsFunction(qtyFuncName, TokenType.INTEGER, new ArgumentType[]{ArgumentType.STRING}, (args, context) -> {
            return containerQty(args, context, inventoryID);
        }));
    }

    private void simpleColorStringFunc(String colorName, Color color) {
        addFunction(new LambdaFixedArgsFunction(colorName, TokenType.STRING, new ArgumentType[]{ArgumentType.ANY}, true, (args, context) -> {
            return Token.fromString(ColorUtil.wrapWithColorTag(args[0].getValue(), color));
        }));
    }
}
