package com.geel.customitemhovers.models.itemtables;

import lombok.Getter;

import java.util.HashMap;

/**
 * A collection of multiple ItemTables for a single item.
 *
 * An item can have multiple ItemTables if multiple ItemTableFiles contain ItemTables for the item.
 */
public class ItemTableCollection {
    @Getter
    private HashMap<String, ItemTable> itemTables;

    public ItemTableCollection() {
        this.itemTables = new HashMap<>();
    }

    public String resolveVariable(String variableName) {
        for (ItemTable i : itemTables.values()) {
            String val = i.getOrDefault(variableName, null);

            if (val == null) {
                continue;
            }

            return val;
        }

        return null;
    }
}
