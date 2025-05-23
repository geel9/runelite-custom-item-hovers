package com.geel.customitemhovers.expressions.providers;

import com.geel.customitemhovers.expressions.ExecutionContext;
import com.geel.customitemhovers.expressions.functions.Function;
import com.geel.customitemhovers.expressions.functions.LambdaFixedArgsFunction;
import com.geel.customitemhovers.expressions.models.ArgumentType;
import com.geel.customitemhovers.expressions.models.Token;
import com.geel.customitemhovers.expressions.models.TokenType;

public abstract class BaseObjectNamespace implements INamespace {

    @Override
    public Function findFunction(String name, Token[] args) {
        switch(name) {
            case "use":
                if(args.length != 1 || args[0].getType() != TokenType.LAMBDA) {
                    break;
                }

                final INamespace thisNamespace = this;
                return new LambdaFixedArgsFunction("use", TokenType.LAMBDA, new ArgumentType[]{ArgumentType.LAMBDA},
                        (tokens, executionContext) -> {
                    Token lambda = tokens[0];

                    // Set the current namespace as the nearest-scoped one
                    // HACK: This is abusing invariance between INamespace and the IProviders.
                    // Create a ChildNamespace() which takes two namespaces??
                    SimpleNonObjectNamespace newScope = new SimpleNonObjectNamespace(thisNamespace, thisNamespace, executionContext.getRootNamespace());
                    ExecutionContext newExecutionContext = new ExecutionContext(newScope);

                    // Execute the lambda in the new scope
                    return newExecutionContext.executeLambda(lambda, new Token[0]);
                });
        }

        return innerFindFunction(name, args);
    }

    protected abstract Function innerFindFunction(String name, Token[] args);
}
