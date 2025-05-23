package com.geel.customitemhovers.expressions.providers;

import com.geel.customitemhovers.expressions.ExecutionContext;
import com.geel.customitemhovers.expressions.functions.Function;
import com.geel.customitemhovers.expressions.functions.LambdaFixedArgsFunction;
import com.geel.customitemhovers.expressions.functions.LambdaVariadicFunction;
import com.geel.customitemhovers.expressions.models.ArgumentType;
import com.geel.customitemhovers.expressions.models.Token;
import com.geel.customitemhovers.expressions.models.TokenType;

import java.util.Arrays;

public abstract class BaseObjectNamespace implements INamespace {

    @Override
    public Function findFunction(String name, Token[] args) {
        switch(name) {
            // use(lambda, args...) -- execute the lambda with the current namespace as the root namespace
            // This allows for, eg, `bank.find(995).use({-> qty * price})`, where `qty` and `price` come from the result of `find(995)`
            // If more than one argument is passed, the rest are passed into the lambda as arguments. This allows
            // users to work around issues where a variable in an inner scope occludes a variable in an outer scope.
            case "use":
                if(args.length == 0 || args[0].getType() != TokenType.LAMBDA) {
                    break;
                }

                final INamespace thisNamespace = this;
                return new LambdaVariadicFunction("use", TokenType.LAMBDA, ArgumentType.ANY, new ArgumentType[]{ArgumentType.LAMBDA},
                        (tokens, executionContext) -> {
                    Token lambda = tokens[0];
                    Token[] inputArgs = Arrays.stream(tokens).skip(1).toArray(Token[]::new);

                    // Set the current namespace as the nearest-scoped one
                    // HACK: This is abusing invariance between INamespace and the IProviders.
                    // Create a ChildNamespace() which takes two namespaces??
                    SimpleNonObjectNamespace newScope = new SimpleNonObjectNamespace(thisNamespace, thisNamespace, executionContext.getRootNamespace());
                    ExecutionContext newExecutionContext = new ExecutionContext(newScope);

                    // Execute the lambda in the new scope
                    return newExecutionContext.executeLambda(lambda, inputArgs);
                });
        }

        return innerFindFunction(name, args);
    }

    protected abstract Function innerFindFunction(String name, Token[] args);
}
