package com.geel.customitemhovers.expressions.models;

import com.geel.customitemhovers.expressions.*;
import com.geel.customitemhovers.expressions.Compiler;
import lombok.Getter;

/**
 * A Variable provided by an IVariableProvider
 */
public class Variable {
    @Getter
    private final VariableType type;

    @Getter
    private final Token constantToken;

//    @Getter
//    private final CompiledExpression expression;

    public Variable(Token token) {
        this.type = VariableType.CONSTANT;
        this.constantToken = token;
//        this.expression = null;
    }

    public Variable(CompiledExpression expression) throws Exception {
        // If the expression is actually just a constant value, we're a constant
        Token[] tokens = expression.getTokens();
        if(tokens.length == 1 && tokens[0].getType().isConstant()) {
            this.type = VariableType.CONSTANT;
            this.constantToken = tokens[0];
//            this.expression = null;
            return;
        }

        // Otherwise, we're an expression
        // TODO: Support expressions in variables
        throw new Exception("a");
//        this.type = VariableType.EXPRESSION;
//        this.constantToken = null;
//        this.expression = expression;
    }

    public static Variable fromConstant(String value) {
        return new Variable(Token.fromString(value));
    }

    public static Variable fromConstant(int value) {
        return new Variable(Token.fromInt(value));
    }

    public static Variable fromConstant(double value) {
        return new Variable(Token.fromDouble(value));
    }

    // TODO: Enable expressions in variables
    public static Variable fromExpressionOrConstantString(String expressionOrConstant) {
        // If not an expression, parse as a constant value
//        if(!expressionOrConstant.startsWith("${") || !expressionOrConstant.endsWith("}")) {
        return new Variable(Evaluator.evaluateStringToToken(expressionOrConstant));
//        }

        // If an expression, parse it out
//        CompiledExpression expression;
//        try {
//            TokenizedExpression tokenized = new Tokenizer(expressionOrConstant, 0).tokenize();
//            expression = Compiler.Compile(tokenized);
//        } catch (Exception e) {
//            return Variable.fromConstant(e.getMessage()); // Probably a bad idea!
//        }
//
//        return new Variable(expression);
    }
}

