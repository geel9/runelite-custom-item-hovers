package com.geel.customitemhovers.expressions.providers;

import com.geel.customitemhovers.expressions.models.Variable;

import java.util.ArrayList;
import java.util.HashMap;

public class SimpleVariableProvider extends HashMap<String, Variable> implements IVariableProvider {
    public SimpleVariableProvider() {

    }

    @Override
    public Variable resolveVariable(String name) {
        return getOrDefault(name, null);
    }
}
