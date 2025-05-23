package com.geel.customitemhovers.namespaces;

import com.geel.customitemhovers.ItemNameMap;
import com.geel.customitemhovers.expressions.ExecutionContext;
import com.geel.customitemhovers.expressions.functions.LambdaVariadicFunction;
import com.geel.customitemhovers.expressions.generators.ChainedGenerator;
import com.geel.customitemhovers.expressions.generators.EmptyGenerator;
import com.geel.customitemhovers.expressions.models.ArgumentType;
import com.geel.customitemhovers.expressions.models.Token;
import com.geel.customitemhovers.expressions.generators.Generator;
import com.geel.customitemhovers.expressions.models.TokenType;
import com.geel.customitemhovers.expressions.providers.SimpleNonObjectNamespace;
import com.geel.customitemhovers.expressions.providers.SimpleObjectNamespace;
import com.geel.customitemhovers.providers.ItemStackVariableProvider;
import net.runelite.api.*;
import net.runelite.client.game.ItemManager;

import java.util.HashSet;
import java.util.Set;

public class InventoryNamespace extends Generator {
    private int index = 0;

    private Item[] items;
    private ItemManager itemManager;

    public InventoryNamespace(int inventoryID, Client client, ItemManager itemManager) {
        super();

        addFunction(new LambdaVariadicFunction("find", TokenType.NAMESPACE, ArgumentType.ANY, new ArgumentType[]{ArgumentType.ANY},
                (args, executionContext) -> {
                    if (args.length == 0) {
                        return null;
                    }

                    Set<Integer> itemIds = parseItemFindArguments(args);
                    if (itemIds.isEmpty()) {
                        return Token.fromGenerator(new EmptyGenerator());
                    }

                    return Token.fromGenerator(new ChainedGenerator(this, (context) -> {
                        Token item = context.getCurrentToken();

                        // Filter the items in the inventory based on the item IDs, returning each one which matches
                        do {
                            if (item.getType() != TokenType.NAMESPACE) {
                                return null;
                            }

                            int itemId = item.getNamespaceValue().resolveVariable("id").getConstantToken().getNumericValue().intValue();
                            if (itemManager != null) itemId = itemManager.canonicalize(itemId);

                            if (itemIds.contains(itemId)) {
                                return item;
                            }
                        } while ((item = context.moveNext()) != null);

                        return null;
                    }));
                }));

        addFunction(new LambdaVariadicFunction("findFirst", TokenType.NAMESPACE, ArgumentType.ANY, new ArgumentType[]{ArgumentType.ANY},
                (args, executionContext) -> {
                    if (args.length == 0) {
                        return null;
                    }

                    Set<Integer> itemIds = parseItemFindArguments(args);
                    if (itemIds.isEmpty()) {
                        return null;
                    }

                    Token item;

                    // Filter the items in the inventory based on the item IDs, returning the first one which matches
                    while ((item = moveNext(executionContext)) != null) {
                        if (item.getType() != TokenType.NAMESPACE) {
                            return null;
                        }

                        int itemId = item.getNamespaceValue().resolveVariable("id").getConstantToken().getNumericValue().intValue();
                        if (itemManager != null) itemId = itemManager.canonicalize(itemId);

                        if (itemIds.contains(itemId)) {
                            return item;
                        }
                    }

                    return null;
                }));

        addFunction(new LambdaVariadicFunction("qty", TokenType.NAMESPACE, ArgumentType.ANY, new ArgumentType[]{ArgumentType.ANY},
                (args, executionContext) -> {
                    if (args.length == 0) {
                        return null;
                    }

                    Set<Integer> itemIds = parseItemFindArguments(args);
                    if (itemIds.isEmpty()) {
                        return null;
                    }

                    // Filter the items in the inventory based on the item IDs, returning the first one which matches
                    int sumQty = 0;
                    Token item;
                    while ((item = moveNext(executionContext)) != null) {
                        if (item.getType() != TokenType.NAMESPACE) {
                            return null;
                        }

                        int itemId = item.getNamespaceValue().resolveVariable("id").getConstantToken().getNumericValue().intValue();
                        if (itemManager != null) itemId = itemManager.canonicalize(itemId);

                        if (itemIds.contains(itemId)) {
                            sumQty += item.getNamespaceValue().resolveVariable("qty").getConstantToken().getNumericValue().intValue();
                        }
                    }

                    return Token.fromInt(sumQty);
                }));

        if (client == null || client.getLocalPlayer() == null) {
            return;
        }

        ItemContainer container = client.getItemContainer(inventoryID);
        if (container == null) {
            return;
        }

        this.itemManager = itemManager;
        items = container.getItems();
    }

    @Override
    public Token moveNext(ExecutionContext executionContext) {
        if (items == null || index >= items.length) {
            return null;
        }

        Item item = items[index++];
        if (item == null || item.getId() == -1) {
            return moveNext(executionContext);
        }

        ItemComposition itemComposition = itemManager.getItemComposition(item.getId());
        return Token.fromNamespace(new SimpleObjectNamespace(new ItemStackVariableProvider(item, itemComposition)));
    }

    private Set<Integer> parseItemFindArguments(Token[] args) {
        // Loop through the arguments, constructing a list of item IDs
        Set<Integer> itemIDs = new HashSet<>(args.length);
        for (Token arg : args) {
            if (arg.getType() == TokenType.STRING) {
                for (int itemId : ItemNameMap.GetItemIDs(arg.getValue())) {
                    itemIDs.add(itemId);
                }
            } else {
                int itemID = arg.getNumericValue().intValue();
                if (itemManager != null) itemID = itemManager.canonicalize(itemID);

                itemIDs.add(itemID);
            }
        }

        return itemIDs;
    }
}
