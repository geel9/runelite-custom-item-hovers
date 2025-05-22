package com.geel.customitemhovers.expressions;

import com.geel.customitemhovers.expressions.functions.Function;
import com.geel.customitemhovers.expressions.models.CompiledExpression;
import com.geel.customitemhovers.expressions.models.Token;
import com.geel.customitemhovers.expressions.models.Variable;
import com.geel.customitemhovers.expressions.models.VariableType;
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

    public Token resolveVariable(String variableName) {
        if (variableProvider == null) {
            return null;
        }

        // Resolve variable
        Variable resolved = variableProvider.resolveVariable(variableName);
        if (resolved == null) {
            return null;
        }

        // If it's not a constant, panic (not possible atm)
        if (resolved.getType() != VariableType.CONSTANT) {
            return null;
        }

        return resolved.getConstantToken();

        // TODO: Support expressions in variables
        /*
        // If it's an expression, evaluate the expression

        // If runtime, just try to evaluate at runtime -- fail if something goes wrong
        if (!this.isStaticEval()) {
            return EvaluationUtil.EvaluateAtRuntime(resolved.getExpression(), this);
        }

        // Otherwise, for static evaluation, try to statically evaluate -- but return null if it doesn't work
        try {
            CompiledExpression staticEvaluated = EvaluationUtil.EvaluateStatically(resolved.getExpression(), this);

            // If it statically evaluates to a single, static token, then we just return that as the result.
            // Otherwise, return null.
            if (staticEvaluated.getTokens().length == 1) {
                Token ret = staticEvaluated.getTokens()[0];
                if (ret.getType().isConstant()) {
                    return ret;
                }
            }
        } catch (Exception e) {
        }

        return null;*/
    }

    public Function resolveFunction(String functionName, Token[] arguments) {
        if (functionProvider == null) {
            return null;
        }

        return functionProvider.findFunction(functionName, arguments);
    }
}
