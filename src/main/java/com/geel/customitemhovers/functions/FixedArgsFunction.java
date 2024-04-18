package com.geel.customitemhovers.functions;

import com.geel.customitemhovers.ItemHoverContext;
import com.geel.customitemhovers.expressions.models.ArgumentType;
import com.geel.customitemhovers.expressions.models.Token;
import com.geel.customitemhovers.expressions.models.TokenType;
import lombok.Getter;

public abstract class FixedArgsFunction extends Function {
    @Getter
    private int numArguments;

    @Getter
    private ArgumentType[] argumentTypes;

    public FixedArgsFunction(String name, TokenType returnType, ArgumentType[] arguments) {
        super(name, returnType);

        this.argumentTypes = arguments;
        this.numArguments = arguments.length;
    }

    public FixedArgsFunction(String name, TokenType returnType, ArgumentType[] arguments, boolean isConstExpr) {
        super(name, returnType, isConstExpr);

        this.argumentTypes = arguments;
        this.numArguments = arguments.length;
    }

    @Override
    public int getArgumentsHashCode() {
        return numArguments;
    }

    public boolean argumentsMatch(Token[] matchArguments) {
        if(matchArguments.length != numArguments) return false;

        for(int i = 0; i < numArguments; i++) {
            if(!argumentTypes[i].fitsTokenType(matchArguments[i].getType()))
                return false;
        }

        return true;
    }

    public Token execute(Token[] arguments, ItemHoverContext context) {
        if(arguments.length != numArguments) {
            throw new IllegalArgumentException("Invalid number of arguments to " + name);
        }

        Token[] coercedArguments = new Token[arguments.length];
        for(int i = 0; i < arguments.length; i++) {
            coercedArguments[i] = argumentTypes[i].coerceTokenToType(arguments[i]);
        }

        return innerExecute(coercedArguments, context);
    }
}
