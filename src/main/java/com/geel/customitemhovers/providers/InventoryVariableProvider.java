package com.geel.customitemhovers.providers;

import com.geel.customitemhovers.expressions.ExecutionContext;
import com.geel.customitemhovers.expressions.models.Variable;
import com.geel.customitemhovers.expressions.providers.LazyVariableProvider;
import com.geel.customitemhovers.namespaces.InventoryNamespace;
import net.runelite.api.*;
import net.runelite.api.gameval.InventoryID;
import net.runelite.client.game.ItemManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Locale;
import java.util.function.Function;

@Singleton
public class InventoryVariableProvider extends LazyVariableProvider {
    private Client client;
    private ItemManager itemManager;

    @Inject
    public InventoryVariableProvider(Client client, ItemManager itemManager) {
        this.client = client;
        this.itemManager = itemManager;

        add("inv", context -> Variable.fromGenerator(new InventoryNamespace(InventoryID.INV, client, itemManager)));
        add("bank", context -> Variable.fromGenerator(new InventoryNamespace(InventoryID.BANK, client, itemManager)));
        add("equipment", context -> Variable.fromGenerator(new InventoryNamespace(InventoryID.WORN, client, itemManager)));
    }
}
