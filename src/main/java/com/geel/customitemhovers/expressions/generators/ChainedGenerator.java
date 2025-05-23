package com.geel.customitemhovers.expressions.generators;

import com.geel.customitemhovers.expressions.ExecutionContext;
import com.geel.customitemhovers.expressions.models.Token;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Function;

public class ChainedGenerator extends Generator {
    private Generator baseGenerator;
    private Function<ChainedGenerationContext, Token> onMoveNext;

    public ChainedGenerator(Generator baseGenerator, Function<ChainedGenerationContext, Token> onMoveNext) {
        super();

        this.baseGenerator = baseGenerator;
        this.onMoveNext = onMoveNext;
    }

    public Token moveNext(ExecutionContext executionContext) {
        Token currentToken = baseGenerator.moveNext(executionContext);
        if (currentToken == null) {
            return null;
        }

        return onMoveNext.apply(new ChainedGenerationContext(executionContext, currentToken, baseGenerator));
    }

    @AllArgsConstructor
    public class ChainedGenerationContext {
        @Getter
        @Setter
        private ExecutionContext executionContext;

        @Getter
        @Setter
        private Token currentToken;

        private Generator baseGenerator;

        public Token moveNext() {
            return (currentToken = baseGenerator.moveNext(executionContext));
        }
    }
}
