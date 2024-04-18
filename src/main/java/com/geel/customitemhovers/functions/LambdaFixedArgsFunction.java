package com.geel.customitemhovers.functions;

import com.geel.customitemhovers.ItemHoverContext;
import com.geel.customitemhovers.expressions.models.ArgumentType;
import com.geel.customitemhovers.expressions.models.Token;
import com.geel.customitemhovers.expressions.models.TokenType;

public class LambdaFixedArgsFunction extends FixedArgsFunction {
    private java.util.function.BiFunction<Token[], ItemHoverContext, Token> executionFunc;

    public LambdaFixedArgsFunction(String name, TokenType returnType, ArgumentType[] arguments,
                                   java.util.function.BiFunction<Token[], ItemHoverContext, Token> executionFunc) {
        super(name, returnType, arguments);
        this.executionFunc = executionFunc;
    }

    public LambdaFixedArgsFunction(String name, TokenType returnType, ArgumentType[] arguments, boolean isConstExpr,
                                   java.util.function.BiFunction<Token[], ItemHoverContext, Token> executionFunc) {
        super(name, returnType, arguments, isConstExpr);
        this.executionFunc = executionFunc;
    }

    protected Token innerExecute(Token[] arguments, ItemHoverContext context) {
        return executionFunc.apply(arguments, context);
    }
}
