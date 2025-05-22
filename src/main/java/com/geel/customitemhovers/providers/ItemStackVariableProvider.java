package com.geel.customitemhovers.providers;

import com.geel.customitemhovers.expressions.models.Variable;
import com.geel.customitemhovers.expressions.providers.LazyVariableProvider;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;

public class ItemStackVariableProvider extends LazyVariableProvider {
    public ItemStackVariableProvider(Item item, ItemComposition itemComposition) {
        // My hope is that Java's lambdas are sanely designed (unlike mine) and this is cheap
        add("id", executionContext -> Variable.fromConstant(item.getId()));
        add("name", executionContext -> Variable.fromConstant(itemComposition.getName()));
        add("qty", executionContext -> Variable.fromConstant(item.getQuantity()));
        add("value", executionContext -> Variable.fromConstant(itemComposition.getPrice()));
        add("high_alch", executionContext -> Variable.fromConstant(itemComposition.getHaPrice()));
    }
}
