package com.geel.customitemhovers.functions;

import com.geel.customitemhovers.ItemHoverContext;
import com.geel.customitemhovers.expressions.models.ArgumentType;
import com.geel.customitemhovers.expressions.models.Token;
import com.geel.customitemhovers.expressions.models.TokenType;

import java.util.function.BiFunction;

public class LambdaVariadicFunction extends VariadicFunction {
    private BiFunction<Token[], ItemHoverContext, Token> executionFunc;

    public LambdaVariadicFunction(String name,
                                  TokenType returnType,
                                  ArgumentType variadicArgumentType,
                                  ArgumentType[] fixedArguments,
                                  BiFunction<Token[], ItemHoverContext, Token> callback
    ) {
        super(name, returnType, variadicArgumentType, fixedArguments);

        this.executionFunc = callback;
    }

    public LambdaVariadicFunction(String name,
                                  TokenType returnType,
                                  ArgumentType variadicArgumentType,
                                  ArgumentType[] fixedArguments,
                                  boolean isConstExpr,
                                  BiFunction<Token[], ItemHoverContext, Token> callback
    ) {
        super(name, returnType, variadicArgumentType, fixedArguments, isConstExpr);

        this.executionFunc = callback;
    }

    protected Token innerExecute(Token[] arguments, ItemHoverContext context) {
        return executionFunc.apply(arguments, context);
    }
}
