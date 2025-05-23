package com.geel.customitemhovers.expressions.providers;

import com.geel.customitemhovers.expressions.ExecutionContext;
import com.geel.customitemhovers.expressions.functions.Function;
import com.geel.customitemhovers.expressions.models.Token;
import com.geel.customitemhovers.expressions.models.Variable;

public class LazyObjectNamespace extends BaseObjectNamespace {
    private final LazyVariableProvider variableProvider;
    private final BaseFunctionProvider functionProvider;

    public LazyObjectNamespace() {
        this.variableProvider = new LazyVariableProvider();
        this.functionProvider = new BaseFunctionProvider();
    }

    @Override
    public Variable resolveVariable(String name) {
        return variableProvider.resolveVariable(name);
    }

    @Override
    protected Function innerFindFunction(String name, Token[] args) {
        return functionProvider.findFunction(name, args);
    }

    public void addFunction(Function function) {
        functionProvider.add(function);
    }

    public void addVariable(String name, java.util.function.Function<ExecutionContext, Variable> callback) {
        variableProvider.add(name, callback);
    }
}
