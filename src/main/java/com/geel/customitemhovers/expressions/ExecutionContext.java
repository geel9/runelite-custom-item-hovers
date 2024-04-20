package com.geel.customitemhovers.expressions;

import com.geel.customitemhovers.expressions.functions.Function;
import com.geel.customitemhovers.expressions.models.Token;
import com.geel.customitemhovers.expressions.providers.IFunctionProvider;
import com.geel.customitemhovers.expressions.providers.IVariableProvider;

/**
 * Context for expression execution -- packages together providers of functions and variables
 */
public class ExecutionContext {
    private final IFunctionProvider functionProvider;
    private final IVariableProvider variableProvider;

    public ExecutionContext(IFunctionProvider functionProvider,
                            IVariableProvider variableProvider) {
        this.functionProvider = functionProvider;
        this.variableProvider = variableProvider;
    }

    public String resolveVariable(String variableName) {
        if(variableProvider == null) {
            return null;
        }
        return variableProvider.resolveVariable(variableName);
    }

    public Function resolveFunction(String functionName, Token[] arguments) {
        if(functionProvider == null) {
            return null;
        }

        return functionProvider.findFunction(functionName, arguments);
    }
}
