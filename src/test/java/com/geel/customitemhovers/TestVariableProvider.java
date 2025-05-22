package com.geel.customitemhovers;

import com.geel.customitemhovers.expressions.models.Variable;
import com.geel.customitemhovers.expressions.providers.IVariableProvider;

import java.util.HashMap;

public class TestVariableProvider extends HashMap<String, Variable> implements IVariableProvider {
    @Override
    public Variable resolveVariable(String name) {
        return getOrDefault(name, null);
    }
}
