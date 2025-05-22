package com.geel.customitemhovers.models.itemtables;

import com.geel.customitemhovers.expressions.models.Variable;
import com.geel.customitemhovers.expressions.providers.IVariableProvider;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * A parsed entry in an ItemTableFile.
 *
 * Contains variables that are "attached" to items
 */
public class ParsedItemTableEntry extends HashMap<String, Variable> implements IVariableProvider {
    @Override
    public Variable resolveVariable(String name) {
        return getOrDefault(name, null);
    }
}
