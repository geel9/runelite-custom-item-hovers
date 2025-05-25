package com.geel.customitemhovers.models.hovers;

import com.geel.customitemhovers.expressions.functions.Function;
import com.geel.customitemhovers.expressions.functions.UserDefinedFunction;
import com.geel.customitemhovers.expressions.models.ArgumentType;
import com.geel.customitemhovers.expressions.models.CompiledExpression;
import lombok.Getter;

public class ParsedHoverFunction {
    @Getter
    public String name;

    @Getter
    public String[] argumentNames;

    @Getter
    public ArgumentType[] argumentTypes; // TODO: Argument class/struct?

    @Getter
    public CompiledExpression expression;

    @Getter
    public Function trueFunction;

    public ParsedHoverFunction(String name, String[] argumentNames, ArgumentType[] argumentTypes, CompiledExpression expression) {
        this.name = name;
        this.argumentNames = argumentNames;
        this.argumentTypes = argumentTypes;
        this.expression = expression;
        this.trueFunction = new UserDefinedFunction(name, argumentNames, argumentTypes, expression);
    }
}
