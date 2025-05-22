package com.geel.customitemhovers.expressions.providers;

import com.geel.customitemhovers.expressions.functions.Function;
import com.geel.customitemhovers.expressions.models.Token;
import com.geel.customitemhovers.expressions.models.Variable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public class SimpleNamespace implements INamespace {

    @Getter
    private final IFunctionProvider functionProvider;

    @Getter
    private final IVariableProvider variableProvider;

    @Getter
    private final INamespace parentNamespace;

    public SimpleNamespace(IFunctionProvider functionProvider,
                           IVariableProvider variableProvider,
                           INamespace parentNamespace) {
        this.functionProvider = functionProvider;
        this.variableProvider = variableProvider;
        this.parentNamespace = parentNamespace;
    }

    public SimpleNamespace(IFunctionProvider functionProvider,
                           IVariableProvider variableProvider) {
        this(functionProvider, variableProvider, null);
    }

    public SimpleNamespace(IFunctionProvider functionProvider) {
        this(functionProvider, null);
    }

    public SimpleNamespace(IVariableProvider variableProvider) {
        this(null, variableProvider);
    }

    public SimpleNamespace(INamespace parentNamespace) {
        this(null, null, parentNamespace);
    }

    @Override
    public Function findFunction(String name, Token[] arguments) {
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
