package com.geel.customitemhovers.expressions.functions;

import com.geel.customitemhovers.expressions.ExecutionContext;
import com.geel.customitemhovers.expressions.models.Token;
import com.geel.customitemhovers.expressions.models.TokenType;
import lombok.Getter;

public abstract class Function {
    @Getter
    protected String name;

    @Getter
    protected TokenType returnType;

    @Getter
    protected boolean isConstExpr;

    public Function(String name, TokenType returnType) {
        this.name = name;
        this.returnType = returnType;
    }

    public Function(String name, TokenType returnType, boolean isConstExpr) {
        this(name, returnType);
        this.isConstExpr = isConstExpr;
    }

    public abstract int getArgumentsHashCode();

    public abstract boolean argumentsMatch(Token[] matchArguments);

    public abstract Token execute(Token[] arguments, ExecutionContext context);

    protected abstract Token innerExecute(Token[] arguments, ExecutionContext context);
}
