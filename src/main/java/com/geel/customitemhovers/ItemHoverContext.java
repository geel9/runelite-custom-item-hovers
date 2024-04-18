package com.geel.customitemhovers;

import com.geel.customitemhovers.functions.FunctionProvider;
import com.geel.customitemhovers.models.itemtables.ItemTable;
import com.geel.customitemhovers.models.itemtables.ItemTableCollection;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;

import java.util.Locale;

/**
 * Context file for an item hover; used to simplify method signatures internally
 */
public class ItemHoverContext {
    @Getter
    private Client client;

    @Getter
    private CustomItemHoversPlugin plugin;

    @Getter
    private Item item;

    @Getter
    private ItemComposition itemComposition;

    @Getter
    private ItemTableCollection tableCollection;

    @Getter
    private FunctionProvider functionProvider;

    @Getter
    private ItemTable baseTable;

    public ItemHoverContext(Client client,
                            CustomItemHoversPlugin plugin,
                            Item item,
                            ItemComposition composition,
                            FunctionProvider functionProvider,
                            ItemTableCollection tableCollection,
                            ItemTable baseTable) {
        this.client = client;
        this.plugin = plugin;
        this.item = item;
        this.itemComposition = composition;
        this.functionProvider = functionProvider;
        this.tableCollection = tableCollection;
        this.baseTable = baseTable;
    }

    public String resolveVariable(String variableName) {
        if(tableCollection != null) {
            variableName = variableName.toLowerCase(Locale.ROOT);

            // Try to resolve via explicit tables, then base table
            String tableCollectionRet = tableCollection.resolveVariable(variableName);
            if (tableCollectionRet != null) {
                return tableCollectionRet;
            }
        }

        if(baseTable == null) {
            return null;
        }

        return baseTable.getOrDefault(variableName, null);
    }
}
