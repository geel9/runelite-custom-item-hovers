package com.geel.customitemhovers.expressions.generators;

import com.geel.customitemhovers.expressions.ExecutionContext;
import com.geel.customitemhovers.expressions.models.Token;

import java.util.function.Function;

public class LambdaGenerator extends Generator {
    private Function<ExecutionContext, Token> onMoveNext;

    public LambdaGenerator(Function<ExecutionContext, Token> onMoveNext) {
        super();
        this.onMoveNext = onMoveNext;
    }

    public Token moveNext(ExecutionContext executionContext) {
        return onMoveNext.apply(executionContext);
    }
}
