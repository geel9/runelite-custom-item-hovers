package com.geel.customitemhovers.functions;

import com.geel.customitemhovers.ItemHoverContext;
import com.geel.customitemhovers.expressions.models.ArgumentType;
import com.geel.customitemhovers.expressions.models.Token;
import com.geel.customitemhovers.expressions.models.TokenType;
import com.sun.org.apache.xpath.internal.Arg;
import lombok.Getter;

public abstract class VariadicFunction extends Function {
    @Getter
    private int minVariadicArguments;

    @Getter
    private int maxVariadicArguments;

    @Getter
    private ArgumentType variadicArgumentType;

    @Getter
    private int numFixedArguments;

    @Getter
    private final ArgumentType[] fixedArgumentTypes;

    public VariadicFunction(String name,
                            TokenType returnType,
                            ArgumentType variadicArgumentType
    ) {
        this(name, returnType, 0, 100, variadicArgumentType, null, false);
    }

    public VariadicFunction(String name,
                            TokenType returnType,
                            ArgumentType variadicArgumentType,
                            boolean isConstExpr
    ) {
        this(name, returnType, 0, 100, variadicArgumentType, null, isConstExpr);
    }

    public VariadicFunction(String name,
                            TokenType returnType,
                            ArgumentType variadicArgumentType,
                            ArgumentType[] fixedArguments
    ) {
        this(name, returnType, 0, 100, variadicArgumentType, fixedArguments, false);
    }

    public VariadicFunction(String name,
                            TokenType returnType,
                            ArgumentType variadicArgumentType,
                            ArgumentType[] fixedArguments,
                            boolean isConstExpr
    ) {
        this(name, returnType, 0, 100, variadicArgumentType, fixedArguments, isConstExpr);
    }

    public VariadicFunction(String name,
                            TokenType returnType,
                            int minVariadicArguments,
                            int maxVariadicArguments,
                            ArgumentType variadicArgumentType,
                            ArgumentType[] fixedArguments,
                            boolean isConstExpr)
    {
        super(name, returnType, isConstExpr);

        this.minVariadicArguments = minVariadicArguments;
        this.maxVariadicArguments = maxVariadicArguments;
        this.variadicArgumentType = variadicArgumentType;

        if(fixedArguments == null) {
            fixedArguments = new ArgumentType[0];
        }

        this.fixedArgumentTypes = fixedArguments;
        this.numFixedArguments = fixedArguments.length;
    }

    @Override
    public int getArgumentsHashCode() {
        return -1;
    }

    public boolean argumentsMatch(Token[] matchArguments) {
        if(matchArguments.length < minVariadicArguments + numFixedArguments) {
            return false;
        }
        if(matchArguments.length > maxVariadicArguments + numFixedArguments) {
            return false;
        }

        // Check all fixed arguments
        for(int i = 0; i < numFixedArguments; i++) {
            if(!fixedArgumentTypes[i].fitsTokenType(matchArguments[i].getType()))
                return false;
        }

        // Check all variadic arguments
        for(int i = numFixedArguments; i < matchArguments.length; i++)
        {
            if(!variadicArgumentType.fitsTokenType(matchArguments[i].getType())) {
                return false;
            }
        }

        return true;
    }

    public Token execute(Token[] arguments, ItemHoverContext context) {
        if(!argumentsMatch(arguments)) {
            throw new IllegalArgumentException("Invalid number of arguments to " + name);
        }

        Token[] coercedArguments = new Token[arguments.length];

        // Coerce all fixed arguments
        for(int i = 0; i < numFixedArguments; i++) {
            coercedArguments[i] = fixedArgumentTypes[i].coerceTokenToType(arguments[i]);
        }

        // Coerce all variadic arguments
        for(int i = numFixedArguments; i < arguments.length; i++) {
            coercedArguments[i] = variadicArgumentType.coerceTokenToType(arguments[i]);
        }

        return innerExecute(coercedArguments, context);
    }
}
