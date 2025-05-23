package com.geel.customitemhovers.expressions.generators;

import com.geel.customitemhovers.expressions.ExecutionContext;
import com.geel.customitemhovers.expressions.functions.LambdaFixedArgsFunction;
import com.geel.customitemhovers.expressions.models.ArgumentType;
import com.geel.customitemhovers.expressions.models.Token;
import com.geel.customitemhovers.expressions.models.TokenType;
import com.geel.customitemhovers.expressions.providers.LazyNonObjectNamespace;
import com.geel.customitemhovers.expressions.providers.LazyObjectNamespace;

public abstract class Generator extends LazyObjectNamespace {
    public Generator() {
        super();

        // Add generator functions
        // TODO: don't do any of this in the constructor, implement INamespace directly and switch() for perf reasons
        addFunction(new LambdaFixedArgsFunction("select", TokenType.GENERATOR,
                new ArgumentType[]{ArgumentType.LAMBDA},
                (args, executionContext) -> {
                    Token selector = args[0];

                    Generator chainedGenerator = new ChainedGenerator(this, generationContext -> {
                        return generationContext.getExecutionContext().executeLambda(selector, new Token[]{generationContext.getCurrentToken()});
                    });

                    return Token.fromNamespace(chainedGenerator);
                }
        ));

        addFunction(new LambdaFixedArgsFunction("filter", TokenType.GENERATOR,
                new ArgumentType[]{ArgumentType.LAMBDA},
                (args, executionContext) -> {
                    Token filter = args[0];

                    Generator chainedGenerator = new ChainedGenerator(this, generationContext -> {
                        Token curToken = generationContext.getCurrentToken();

                        do {
                            Token filterResult = generationContext.getExecutionContext().executeLambda(filter, new Token[]{curToken});

                            if(filterResult.getBooleanValue()) {
                                return curToken;
                            }
                        } while ((curToken = generationContext.moveNext()) != null);

                        return null;
                    });

                    return Token.fromNamespace(chainedGenerator);
                }
        ));

        addFunction(new LambdaFixedArgsFunction("first", TokenType.NAMESPACE, new ArgumentType[]{ArgumentType.LAMBDA},
                (args, executionContext) -> {
                    Token selector = args[0];
                    Token curToken;

                    while((curToken = moveNext(executionContext)) != null) {
                        Token result = executionContext.executeLambda(selector, new Token[]{curToken});

                        if (result.getBooleanValue()) {
                            return curToken;
                        }
                    }

                    return null;
                }
        ));

        addFunction(new LambdaFixedArgsFunction("any", TokenType.INTEGER, new ArgumentType[]{ArgumentType.LAMBDA},
                (args, executionContext) -> {
                    Token selector = args[0];
                    Token curToken;

                    while((curToken = moveNext(executionContext)) != null) {
                        Token result = executionContext.executeLambda(selector, new Token[]{curToken});

                        if (result.getBooleanValue()) {
                            return Token.fromBool(true);
                        }
                    }

                    return Token.fromBool(false);
                }
        ));

        addFunction(new LambdaFixedArgsFunction("sum", TokenType.DOUBLE, new ArgumentType[0],
                (args, executionContext) -> {
                    double sum = 0;
                    Token curVal;
                    while ((curVal = moveNext(executionContext)) != null) {
                        // TODO: Does not handle non-numeric values
                        sum += curVal.getNumericValue().doubleValue();
                    }

                    return Token.fromDouble(sum);
                }
        ));

        addFunction(new LambdaFixedArgsFunction("count", TokenType.DOUBLE, new ArgumentType[0],
                (args, executionContext) -> {
                    int count = 0;
                    while ((moveNext(executionContext)) != null) {
                        count++;
                    }

                    return Token.fromInt(count);
                }
        ));
    }

    public abstract Token moveNext(ExecutionContext executionContext);
}
