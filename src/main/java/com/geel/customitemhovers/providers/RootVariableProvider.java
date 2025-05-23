package com.geel.customitemhovers.providers;

import com.geel.customitemhovers.expressions.models.Variable;
import com.geel.customitemhovers.expressions.providers.IVariableProvider;
import com.geel.customitemhovers.expressions.providers.MultiVariableProvider;
import com.geel.customitemhovers.models.itemtables.ParsedItemTableEntry;
import com.geel.customitemhovers.models.itemtables.ItemTableCollection;
import lombok.Getter;

import java.util.Locale;

/**
 * Provides variables for an item hover.
 *
 * Basically just packages together an ItemTableCollection (containing variables that the user of the plugin has
 * set up) and a base ItemTable (generated for each item that is hovered over).
 */
public class RootVariableProvider extends MultiVariableProvider {
    @Getter
    private ItemTableCollection tableCollection;

    @Getter
    private PlayerVariableProvider playerVariableProvider;

    @Getter
    private ItemStackVariableProvider itemStackVariableProvider;

    @Getter
    private InventoryVariableProvider inventoryVariableProvider;

    public RootVariableProvider(ItemTableCollection tableCollection,
                                PlayerVariableProvider playerVariableProvider,
                                ItemStackVariableProvider itemStackVariableProvider,
                                InventoryVariableProvider inventoryVariableProvider) {
        super(tableCollection, playerVariableProvider);
        this.Add(itemStackVariableProvider);
        this.Add(inventoryVariableProvider);
    }
}
