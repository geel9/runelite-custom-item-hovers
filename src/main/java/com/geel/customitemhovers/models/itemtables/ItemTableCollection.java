package com.geel.customitemhovers.models.itemtables;

import com.geel.customitemhovers.expressions.models.Variable;
import com.geel.customitemhovers.expressions.providers.IVariableProvider;
import lombok.Getter;

import java.util.HashMap;

/**
 * A collection of multiple ItemTables for a single item.
 *
 * An item can have multiple ItemTables if multiple ItemTableFiles contain ItemTables for the item.
 */
public class ItemTableCollection implements IVariableProvider {
    @Getter
    private HashMap<String, ParsedItemTableEntry> itemTables;

    public ItemTableCollection() {
        this.itemTables = new HashMap<>();
    }

    @Override
    public Variable resolveVariable(String variableName) {
        for (ParsedItemTableEntry i : itemTables.values()) {
            Variable val = i.getOrDefault(variableName, null);

            if (val == null) {
                continue;
            }

            return val;
        }

        return null;
    }
}
