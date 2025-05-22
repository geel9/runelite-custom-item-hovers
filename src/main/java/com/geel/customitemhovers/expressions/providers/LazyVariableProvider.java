package com.geel.customitemhovers.expressions.providers;

import com.geel.customitemhovers.expressions.ExecutionContext;
import com.geel.customitemhovers.expressions.models.Variable;

import java.util.HashMap;
import java.util.function.Function;

public class LazyVariableProvider implements IVariableProvider {
    // Map between a string and a lambda which returns a Variable
    private HashMap<String, Function<ExecutionContext, Variable>> variableCallbacks = new HashMap<>();

    public LazyVariableProvider() {
    }

    public LazyVariableProvider add(String name, Function<ExecutionContext, Variable> callback) {
        variableCallbacks.put(name, callback);
        return this;
    }

    @Override
    public Variable resolveVariable(String name) {
        Function<ExecutionContext, Variable> callback = variableCallbacks.get(name);
        if (callback != null) {
            return callback.apply(null);
        }
        return null;
    }
}
