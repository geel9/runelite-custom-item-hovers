package com.geel.customitemhovers.expressions.functions;

import com.geel.customitemhovers.expressions.ExecutionContext;
import com.geel.customitemhovers.expressions.models.ArgumentType;
import com.geel.customitemhovers.expressions.models.CompiledExpression;
import com.geel.customitemhovers.expressions.models.Token;
import com.geel.customitemhovers.expressions.models.TokenType;

public class UserDefinedFunction extends FixedArgsFunction {
    public CompiledExpression backingExpression;

    private Token lambdaToken;

    public UserDefinedFunction(String name, String[] argumentNames, ArgumentType[] argumentTypes, CompiledExpression backingExpression) {
        super(name, TokenType.INTEGER, argumentTypes);
        this.backingExpression = backingExpression;

        // TODO: Allocating a new Token here is silly, we have everything we need already
        //       Turns out designing languages well is harder than I expected :~)
        Token lambdaToken = new Token(TokenType.LAMBDA, name);
        lambdaToken.setLambdaArgs(argumentNames);
        lambdaToken.setCompiledLambda(backingExpression);

        this.lambdaToken = lambdaToken;
    }

    @Override
    protected Token innerExecute(Token[] arguments, ExecutionContext context) {
        return context.executeLambda(lambdaToken, arguments);
    }
}
