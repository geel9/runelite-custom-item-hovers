package com.geel.customitemhovers.expressions;

import com.geel.customitemhovers.expressions.functions.Function;
import com.geel.customitemhovers.expressions.models.CompiledExpression;
import com.geel.customitemhovers.expressions.models.Token;
import com.geel.customitemhovers.expressions.providers.IFunctionProvider;
import com.geel.customitemhovers.expressions.providers.IVariableProvider;
import lombok.Getter;

/**
 * Context for expression execution -- packages together providers of functions and variables
 */
public class ExecutionContext {
    @Getter
    private final IFunctionProvider functionProvider;

    @Getter
    private final IVariableProvider variableProvider;

    @Getter
    private final boolean staticEval;

    public ExecutionContext(IFunctionProvider functionProvider,
                            IVariableProvider variableProvider) {
        this.functionProvider = functionProvider;
        this.variableProvider = variableProvider;
        this.staticEval = false;
    }

    public ExecutionContext(ExecutionContext other, boolean staticEval) {
        this.functionProvider = other.getFunctionProvider();
        this.variableProvider = other.getVariableProvider();
        this.staticEval = staticEval;
    }

    public String resolveVariable(String variableName) {
        if(variableProvider == null) {
            return null;
        }

        // Resolve variable
        return variableProvider.resolveVariable(variableName);
    }

    public Function resolveFunction(String functionName, Token[] arguments) {
        if (functionProvider == null) {
            return null;
        }

        return functionProvider.findFunction(functionName, arguments);
    }
}
