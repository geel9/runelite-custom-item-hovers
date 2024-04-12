package com.geel.customitemhovers;

import lombok.Getter;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;

import java.util.Locale;

/**
 * Context file for an item hover; used to simplify method signatures internally
 */
public class ItemHoverContext {
    @Getter
    private Item item;

    @Getter
    private ItemComposition itemComposition;

    @Getter
    private ItemTableCollection tableCollection;

    @Getter
    private ItemTable baseTable;

    public ItemHoverContext(Item item, ItemComposition composition, ItemTableCollection tableCollection, ItemTable baseTable) {
        this.item = item;
        this.itemComposition = composition;
        this.tableCollection = tableCollection;
        this.baseTable = baseTable;
    }

    public String resolveVariable(String variableName) {
        variableName = variableName.toLowerCase(Locale.ROOT);

        // Try to resolve via explicit tables, then base table
        String tableCollectionRet = tableCollection.resolveVariable(variableName);
        if(tableCollectionRet != null)
        {
            return tableCollectionRet;
        }

        return baseTable.getOrDefault(variableName, null);
    }
}
