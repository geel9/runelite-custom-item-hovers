package com.geel.customitemhovers.namespaces;

import com.geel.customitemhovers.expressions.ExecutionContext;
import com.geel.customitemhovers.expressions.models.Token;
import com.geel.customitemhovers.expressions.generators.Generator;
import com.geel.customitemhovers.expressions.providers.SimpleNamespace;
import com.geel.customitemhovers.providers.ItemStackVariableProvider;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.client.game.ItemManager;

public class InventoryNamespace extends Generator {
    private int index = 0;

    private Item[] items;
    private ItemManager itemManager;

    public InventoryNamespace(int inventoryID, Client client, ItemManager itemManager) {
        super();
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
        if (item == null) {
            return moveNext(executionContext);
        }

        ItemComposition itemComposition = itemManager.getItemComposition(item.getId());
        return Token.fromNamespace(new SimpleNamespace(new ItemStackVariableProvider(item, itemComposition)));
    }
}
