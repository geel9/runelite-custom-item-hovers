package com.geel.customitemhovers.expressions.providers;

import com.geel.customitemhovers.expressions.functions.Function;
import com.geel.customitemhovers.expressions.models.Token;
import com.geel.customitemhovers.expressions.models.Variable;
import lombok.Getter;

public class SimpleObjectNamespace extends BaseObjectNamespace {

    @Getter
    private final IFunctionProvider functionProvider;

    @Getter
    private final IVariableProvider variableProvider;

    @Getter
    private final INamespace parentNamespace;

    public SimpleObjectNamespace(IFunctionProvider functionProvider,
                                 IVariableProvider variableProvider,
                                 INamespace parentNamespace) {
        this.functionProvider = functionProvider;
        this.variableProvider = variableProvider;
        this.parentNamespace = parentNamespace;
    }

    public SimpleObjectNamespace(IFunctionProvider functionProvider,
                                 IVariableProvider variableProvider) {
        this(functionProvider, variableProvider, null);
    }

    public SimpleObjectNamespace(IFunctionProvider functionProvider) {
        this(functionProvider, null);
    }

    public SimpleObjectNamespace(IVariableProvider variableProvider) {
        this(null, variableProvider);
    }

    public SimpleObjectNamespace(INamespace parentNamespace) {
        this(null, null, parentNamespace);
    }

    @Override
    public Function innerFindFunction(String name, Token[] arguments) {
        Function function = functionProvider == null ? null : functionProvider.findFunction(name, arguments);

        if (function == null && parentNamespace != null) {
            function = parentNamespace.findFunction(name, arguments);
        }

        return function;
    }

    @Override
    public Variable resolveVariable(String name) {
        Variable variable = variableProvider == null ? null : variableProvider.resolveVariable(name);

        if (variable == null && parentNamespace != null) {
            variable = parentNamespace.resolveVariable(name);
        }

        return variable;
    }
}
