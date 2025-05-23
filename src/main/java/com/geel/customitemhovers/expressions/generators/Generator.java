package com.geel.customitemhovers.expressions.generators;

import com.geel.customitemhovers.expressions.ExecutionContext;
import com.geel.customitemhovers.expressions.functions.LambdaFixedArgsFunction;
import com.geel.customitemhovers.expressions.functions.LambdaVariadicFunction;
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

        // select(lambda)
        // Consumes a generator and feeds each token into `lambda`, emitting the output
        // This allows users to transform streams of objects in a functional way.
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

        // filter(lambda)
        // Consumes a generator and feeds each token into `lambda`, emitting the token if the lambda returns a truthy value
        // This allows users to filter streams of objects
        addFunction(new LambdaFixedArgsFunction("filter", TokenType.GENERATOR,
                new ArgumentType[]{ArgumentType.LAMBDA},
                (args, executionContext) -> {
                    Token filter = args[0];

                    Generator chainedGenerator = new ChainedGenerator(this, generationContext -> {
                        Token curToken = generationContext.getCurrentToken();

                        do {
                            Token filterResult = generationContext.getExecutionContext().executeLambda(filter, new Token[]{curToken});

                            if (filterResult.getBooleanValue()) {
                                return curToken;
                            }
                        } while ((curToken = generationContext.moveNext()) != null);

                        return null;
                    });

                    return Token.fromNamespace(chainedGenerator);
                }
        ));

        // first(lambda?)
        // Consumes a generator and feeds each token into `lambda`, returning the first token that returns a truthy value
        // If no lambda is passed, just returns the first token
        addFunction(new LambdaVariadicFunction("first", TokenType.NAMESPACE, ArgumentType.LAMBDA, new ArgumentType[0],
                (args, executionContext) -> {
                    Token selector = args.length > 0 ? args[0] : null;
                    Token curToken;

                    while ((curToken = moveNext(executionContext)) != null) {
                        // If no selector, we just return the first one (literally)
                        if (selector == null) {
                            return curToken;
                        }

                        Token result = executionContext.executeLambda(selector, new Token[]{curToken});
                        if (result.getBooleanValue()) {
                            return curToken;
                        }
                    }

                    return null;
                }
        ));

        // last(lambda?)
        // Consumes a generator and feeds each token into `lambda`, returning the last token that returns a truthy value
        // If no lambda is passed, just returns the last token
        addFunction(new LambdaVariadicFunction("last", TokenType.NAMESPACE, ArgumentType.LAMBDA, new ArgumentType[0],
                (args, executionContext) -> {
                    Token selector = args.length > 0 ? args[0] : null;
                    Token lastToken = null;

                    while (true) {
                        // Generate next token
                        Token nextToken = moveNext(executionContext);
                        if (nextToken == null) {
                            break;
                        }

                        // If selector exists, evaluate it
                        if (selector != null) {
                            Token result = executionContext.executeLambda(selector, new Token[]{nextToken});

                            if (!result.getBooleanValue()) {
                                continue;
                            }
                        }

                        lastToken = nextToken;
                    }

                    return lastToken;
                }
        ));

        // any(lambda?)
        // Consumes a generator and feeds each token into `lambda`, returning true if any token returns a truthy value
        // If no lambda is passed, just returns true if the generator has any tokens
        addFunction(new LambdaFixedArgsFunction("any", TokenType.INTEGER, new ArgumentType[]{ArgumentType.LAMBDA},
                (args, executionContext) -> {
                    Token selector = args.length > 0 ? args[0] : null;
                    Token curToken;

                    while ((curToken = moveNext(executionContext)) != null) {
                        // If no selector, we just return the first one (literally)
                        if (selector == null) {
                            return Token.fromBool(true);
                        }

                        Token result = executionContext.executeLambda(selector, new Token[]{curToken});

                        if (result.getBooleanValue()) {
                            return Token.fromBool(true);
                        }
                    }

                    return Token.fromBool(false);
                }
        ));

        // all(lambda)
        // Consumes a generator and feeds each token into `lambda`, returning true if all tokens return a truthy value
        addFunction(new LambdaFixedArgsFunction("all", TokenType.INTEGER, new ArgumentType[]{ArgumentType.LAMBDA},
                (args, executionContext) -> {
                    Token selector = args[0];

                    Token curToken;
                    while ((curToken = moveNext(executionContext)) != null) {
                        Token result = executionContext.executeLambda(selector, new Token[]{curToken});

                        if (!result.getBooleanValue()) {
                            return Token.fromBool(false);
                        }
                    }

                    return Token.fromBool(true);
                }
        ));

        // max(lambda?)
        // Consumes a generator and returns the largest token.
        // If a lambda is passed, it will be evaluated for each token, and the output will be used as the comparator instead of the token itself
        addFunction(new LambdaVariadicFunction("max", TokenType.DOUBLE, ArgumentType.LAMBDA, new ArgumentType[0],
                (args, executionContext) -> {
                    Token selector = args.length > 0 ? args[0] : null;
                    if (selector != null && selector.getType() != TokenType.LAMBDA) {
                        throw new IllegalArgumentException("Selector must be a lambda");
                    }

                    double maxVal = Double.MIN_VALUE;
                    Token selectedToken = null;

                    Token curToken;
                    while ((curToken = moveNext(executionContext)) != null) {
                        Token compareToken = curToken;

                        // If we have to transform it first, do so
                        if (selector != null) {
                            compareToken = executionContext.executeLambda(selector, new Token[]{curToken});
                        }

                        double curVal = compareToken.getNumericValue().doubleValue();
                        if (curVal > maxVal) {
                            maxVal = curVal;
                            selectedToken = curToken;
                        }
                    }

                    return selectedToken;
                }
        ));

        // min(lambda?)
        // Consumes a generator and returns the smallest token.
        // If a lambda is passed, it will be evaluated for each token, and the output will be used as the comparator instead of the token itself
        addFunction(new LambdaVariadicFunction("min", TokenType.DOUBLE, ArgumentType.LAMBDA, new ArgumentType[0],
                (args, executionContext) -> {
                    Token selector = args.length > 0 ? args[0] : null;
                    if (selector != null && selector.getType() != TokenType.LAMBDA) {
                        throw new IllegalArgumentException("Selector must be a lambda");
                    }

                    double minVal = Double.MAX_VALUE;
                    Token selectedToken = null;

                    Token curToken;
                    while ((curToken = moveNext(executionContext)) != null) {
                        Token compareToken = curToken;

                        // If we have to transform it first, do so
                        if (selector != null) {
                            compareToken = executionContext.executeLambda(selector, new Token[]{curToken});
                        }

                        double curVal = compareToken.getNumericValue().doubleValue();
                        if (curVal < minVal) {
                            minVal = curVal;
                            selectedToken = curToken;
                        }
                    }

                    return selectedToken;
                }
        ));

        // sum()
        // Consumes a generator and returns the sum of all tokens.
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

        // count(lambda?)
        // Consumes a generator and returns the number of tokens which pass the lambda
        // If no lambda is passed, just returns the number of tokens
        addFunction(new LambdaVariadicFunction("count", TokenType.DOUBLE, ArgumentType.LAMBDA, new ArgumentType[0],
                (args, executionContext) -> {
                    Token selector = args.length > 0 ? args[0] : null;

                    int count = 0;
                    while ((moveNext(executionContext)) != null) {
                        // If selector is passed, evaluate it as sugar so people don't have to call .filter() first
                        if (selector != null) {
                            Token result = executionContext.executeLambda(selector, new Token[]{moveNext(executionContext)});
                            if (!result.getBooleanValue()) {
                                continue;
                            }
                        }

                        count++;
                    }

                    return Token.fromInt(count);
                }
        ));
    }

    public abstract Token moveNext(ExecutionContext executionContext);
}
