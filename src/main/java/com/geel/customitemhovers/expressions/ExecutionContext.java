package com.geel.customitemhovers.expressions;

import com.geel.customitemhovers.expressions.functions.Function;
import com.geel.customitemhovers.expressions.models.*;
import com.geel.customitemhovers.expressions.providers.*;
import lombok.Getter;

/**
 * Context for expression execution -- packages together providers of functions and variables
 */
public class ExecutionContext implements INamespace {
    @Getter
    private final INamespace rootNamespace;

    @Getter
    private final boolean staticEval;

    public ExecutionContext(INamespace rootNamespace) {
        this.rootNamespace = rootNamespace;
        this.staticEval = false;
    }

    public ExecutionContext(IFunctionProvider functionProvider, IVariableProvider variableProvider) {
        this.rootNamespace = new SimpleNamespace(functionProvider, variableProvider);
        this.staticEval = false;
    }

    public ExecutionContext(ExecutionContext other, boolean staticEval) {
        this.rootNamespace = other.rootNamespace;
        this.staticEval = staticEval;
    }

    public Token executeLambdaNoArguments(Token lambdaToken) {
        return executeLambda(lambdaToken, new Token[0]);
    }

    public Token executeLambda(Token lambdaToken, Token[] arguments) {
        if (lambdaToken.getType() != TokenType.LAMBDA) {
            throw new IllegalArgumentException("Token is not a lambda");
        }

        // Create a variable provider for the lambda
        SimpleVariableProvider newScopeProvider = new SimpleVariableProvider();

        // Set up the scoped variables
        for(int lambdaArgIndex = 0; lambdaArgIndex < lambdaToken.getLambdaArgs().length; lambdaArgIndex++) {
            String argName = lambdaToken.getLambdaArgs()[lambdaArgIndex];

            // If an argument the lambda is expecting is being passed in arguments(), pass it -- otherwise, set it to 0 (gross)
            if(lambdaArgIndex < arguments.length) {
                Token arg = arguments[lambdaArgIndex];
                newScopeProvider.put(argName, new Variable(arg));
            } else {
                newScopeProvider.put(argName, Variable.fromConstant(0)); // TODO: Null? Shoot me.
            }
        }

        // Create a new namespace for the execution
        INamespace newNamespace = new SimpleNamespace(null, newScopeProvider, rootNamespace);
        ExecutionContext newContext = new ExecutionContext(newNamespace);

        // Execute the lambda
        return EvaluationUtil.EvaluateAtRuntime(lambdaToken.getCompiledLambda(), newContext);

        // TODO: Do we assert that the result must have a certain type?
    }

    @Override
    public Variable resolveVariable(String variableName) {
        return rootNamespace.resolveVariable(variableName);
    }

    @Override
    public Function findFunction(String functionName, Token[] arguments) {
        return rootNamespace.findFunction(functionName, arguments);
    }
}
