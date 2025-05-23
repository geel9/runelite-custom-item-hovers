package com.geel.customitemhovers.expressions.generators;

import com.geel.customitemhovers.expressions.ExecutionContext;
import com.geel.customitemhovers.expressions.models.Token;

import java.util.function.Function;

public class EmptyGenerator extends Generator {
    public EmptyGenerator() {
        super();
    }

    public Token moveNext(ExecutionContext executionContext) {
        return null;
    }
}
