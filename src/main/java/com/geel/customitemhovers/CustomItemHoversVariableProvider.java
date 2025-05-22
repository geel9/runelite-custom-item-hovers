package com.geel.customitemhovers;

import com.geel.customitemhovers.expressions.models.Variable;
import com.geel.customitemhovers.expressions.providers.IVariableProvider;
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
public class CustomItemHoversVariableProvider implements IVariableProvider {
    @Getter
    private ItemTableCollection tableCollection;

    @Getter
    private ParsedItemTableEntry baseTable;

    public CustomItemHoversVariableProvider(
                                      ItemTableCollection tableCollection,
                                      ParsedItemTableEntry baseTable) {
        this.tableCollection = tableCollection;
        this.baseTable = baseTable;
    }

    public Variable resolveVariable(String variableName) {
        if(tableCollection != null) {
            variableName = variableName.toLowerCase(Locale.ROOT);

            // Try to resolve via explicit tables, then base table
            Variable tableCollectionRet = tableCollection.resolveVariable(variableName);
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
