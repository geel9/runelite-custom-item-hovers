package com.geel.customitemhovers;

import com.geel.customitemhovers.expressions.*;
import com.geel.customitemhovers.expressions.Compiler;
import com.geel.customitemhovers.expressions.functions.LambdaFixedArgsFunction;
import com.geel.customitemhovers.expressions.models.*;
import com.geel.customitemhovers.expressions.providers.LazyNamespace;
import com.geel.customitemhovers.providers.RootFunctionProvider;
import org.junit.Test;

import static org.junit.Assert.*;

public class ExpressionTest {
    private static double EPSILON = 0.000001;
    private static ExecutionContext context;

    @Test
    public void TestTokenizer() {
        // Prepare execution context
        TestVariableProvider variableProvider = new TestVariableProvider();
        variableProvider.put("a", Variable.fromConstant("b"));
        variableProvider.put("b", Variable.fromConstant(2));
        variableProvider.put("c", Variable.fromConstant(4));

        LazyNamespace namespace = new LazyNamespace();
        namespace.addVariable("blah", (context) -> {
            return Variable.fromConstant("5");
        });
        variableProvider.put("namespaceVar", Variable.fromNamespace(namespace));

        RootFunctionProvider functionProvider = new RootFunctionProvider(null, null);

        // Add methods for testing lambdas
        // eval() -- just takes in an expression and executes it, returning the result
        functionProvider.add(new LambdaFixedArgsFunction("eval", TokenType.LAMBDA,
                new ArgumentType[]{ArgumentType.LAMBDA}, (args, context) -> {
            Token lambdaToken = args[0];

            // Execute the expression within the current context
            return context.executeLambdaNoArguments(lambdaToken);
        }));

        // range_sum() -- takes in an expression, rStart, and rEnd. Loops i from rStart to rEnd, evaluating the expression,
        // returning the sum of all the results
        functionProvider.add(new LambdaFixedArgsFunction("range_sum", TokenType.DOUBLE,
                new ArgumentType[]{ArgumentType.LAMBDA, ArgumentType.INTEGER, ArgumentType.INTEGER},
                (args, context) -> {
                    Token lambdaToken = args[0];
                    int start = args[1].getNumericValue().intValue();
                    int end = args[2].getNumericValue().intValue();

                    double sum = 0;
                    for (int i = start; i <= end; i++) {
                        Token result = context.executeLambda(lambdaToken, new Token[]{
                                Token.fromInt(i),
                                Token.fromDouble(sum)
                        });

                        sum += result.getNumericValue().doubleValue();
                    }

                    return Token.fromDouble(sum);
                }));

        context = new ExecutionContext(functionProvider, variableProvider);

        // namespace()
        testStringExpr("namespaceFunc('5').blah", "5");
        testStringExpr("namespaceVar.blah", "5");

        // eval()
        testStringExpr("eval({-> redStart()})", "<col=ff0000>");

        // range_sum()
        testDoubleExpr("range_sum({i -> i % 2 == 0 ? pow(i, 2) : 0}, 0, 10)", 220);
        testDoubleExpr("range_sum({i, sum -> i % 2 == 0 ? pow(i, 2) : sum}, 0, 10)", 564);
        testDoubleExpr("range_sum({i, sum, alwaysZeroBecauseNotPassed -> alwaysZeroBecauseNotPassed != 0 ? 100000 : (i % 2 == 0 ? pow(i, 2) : sum)}, 0, 10)",
                564);

        //range_sum() with nested range_sum, testing that `i` and `sum` are properly scoped
        testDoubleExpr("range_sum({i, sum -> range_sum({i, sum -> sum != 0 ? 10000 : 0}, 0, 5) + 1}, 0, 10)", 11); // Because both lambdas declare i and sum, they are scoped to the lambda
        testDoubleExpr("range_sum({i, sum -> range_sum({i -> sum != 0 ? 10000 : 0}, 0, 5) + 1}, 0, 10)", 600011.0); // Because the inner lambda doesn't declare sum, it inherits the outer sum.
        testDoubleExpr("range_sum({i, sum -> range_sum({j, k, sum -> sum != 0 ? 10000 : 0}, 0, 5) + 1}, 0, 10)", 11.0); // Because the inner lambda does declare sum, and it's not passed by the caller, it is always 0


        // Functions
        testStringExpr("red(a)", "<col=ff0000>b</col>");
        testStringExpr("green('a')", "<col=00ff00>a</col>");
        testStringExpr("blue('a')", "<col=0000ff>a</col>");
        testStringExpr("rgb(255, pow(2, 8) - 1, 2, 'hi')", "<col=ffff02>hi</col>");
        testStringExpr("rgb(255, 255, 255, 'hi')", "<col=ffffff>hi</col>");
        testDoubleExpr("pow(2, 3)", 8);
        testDoubleExpr("pow(2, 3.0)", 8);
        testDoubleExpr("pow(2.0, 3)", 8);
        testDoubleExpr("pow(2.0, 3.0)", 8);
        testDoubleExpr("pow(2, 3.0000000000001)", 8);
        testDoubleExpr("max(1,2,15, 2.3, 15.4, (15 + 0.5 + (1/3)), (15 + 0.5 - (2 * 1/3)))", 15.833333333333);
        testDoubleExpr("min(1,2,15, 2.3, 15.4, (15 + 0.5 + (1/3)), (15 - (201 * 1/3)))", -52);
        testDoubleExpr("min((15 - (201 * 1/3)))", -52);
        testDoubleExpr("max((15 + 0.5 + (1/3)), (15 - (201 * 1/3)))", 15.833333333333);
        testDoubleExpr("max(1,2,15, 2.3, 15.4, (15 + 0.5 + (1/3)), (15 - (201 * 1/3)))", 15.833333333333);
        testDoubleExpr("max(1,2,15, 2.3, 15.4, (15 + 0.5 + (1/3)), (15 - (201 * 1/3)), 0)", 15.833333333333);
        testDoubleExpr("min(1,2,15, 2.3, 15.4, (15 + 0.5 + (1/3)), (15 - (201 * 1/3)), 0)", -52);
        testDoubleExpr("min(1,2,15, 2.3, 15.4, (15 + 0.5 + (1/3)), (15 - (201 * 1/3)), 0, 100)", -52);
        testDoubleExpr("max(1,2,15, 2.3, 15.4, (15 + 0.5 + (1/3)), (15 - (201 * 1/3)), 0, 100)", 100);


        // Arithmetic
        testIntExpr("0 + 0", 0);
        testIntExpr("0 + 1", 1);
        testIntExpr("1 + 1", 2);
        testDoubleExpr("1.1 + 1.01", 2.11);
        testIntExpr("0 - 0", 0);
        testIntExpr("0 - 1", -1);
        testIntExpr("1 - 1", 0);
        testDoubleExpr("1.1 - 1.01", 0.09);
        testDoubleExpr("1 - 1.01", -0.01);
        testDoubleExpr("1.0 - 1", 0);
        testDoubleExpr("1 - 1.0", 0);
        testDoubleExpr("1.0 - 1.0000000000001", 0);
        testDoubleExpr("1.0000000000001 - 1", 0);
        testDoubleExpr("1.0000000000001 - 1.0000000000001", 0);

        // Multiplication/division
        testIntExpr("0 * 0", 0);
        testIntExpr("0 * 1", 0);
        testIntExpr("1 * 0", 0);
        testIntExpr("1 * 1", 1);
        testIntExpr("1 * 2", 2);
        testIntExpr("2.0 * 1", 2);
        testIntExpr("(1 + 3) * (4.5 * 2)", 36);
        testIntExpr("0 / 1", 0);
        testIntExpr("1 / 1", 1);
        testDoubleExpr("1 / 2", 0.5);
        testIntExpr("2 / 1", 2);
        testDoubleExpr("(1 + 3) / (4.5 * 2)", 4d / 9d);
        testDoubleExpr("1 / 2.0", 0.5);
        testDoubleExpr("1.0 / 2", 0.5);
        testDoubleExpr("1.0 / 2.0", 0.5);
        testDoubleExpr("1.0 / 2.0000000000001", 0.4999999999999999);
        testDoubleExpr("1.0000000000001 / 2", 0.5000000000000002);


        // Modulo
        testIntExpr("0 % 1", 0);
        testIntExpr("0 % 15", 0);
        testIntExpr("1 % 1", 0);
        testIntExpr("1 % 15", 1);
        testIntExpr("14 % 15", 14);
        testIntExpr("15 % 15", 0);
        testIntExpr("18 % 15", 3);
        testIntExpr("18 % (0 - 5)", 3);

        // Functions
        testIntExpr("ceil(0) == 0", 1);
        testIntExpr("floor(0) == 0", 1);
        testIntExpr("floor(0) == ceil(0)", 1);
        testIntExpr("floor(0.01) != ceil(0.01)", 1);
        testIntExpr("floor(2)", 2);
        testIntExpr("ceil(2)", 2);
        testDoubleExpr("floor(2.0)", 2);
        testDoubleExpr("ceil(2.0)", 2);
        testDoubleExpr("floor(2.1)", 2);
        testDoubleExpr("ceil(2.1)", 3);
        testDoubleExpr("floor((0.01 + 15.03))", 15);
        testDoubleExpr("ceil((0.01 + 15.03))", 16);
        testDoubleExpr("floor(0.01 + 15.03)", 15);
        testDoubleExpr("ceil(0.01 + 15.03)", 16);
        testDoubleExpr("floor(0.01 + 15.03) == ceil(0.01 + 15.03)", 0);

        // Boolean tests
        testIntExpr("((1 + 3) * (4.5 * 2)) == 36", 1);
        testIntExpr("((1 + 3) >= (0 + 2)) && ((1 - 4) == (0 - 3))", 1);
        testIntExpr("((1 + 3) >= (15 + 5)) && ((1 - 4) == (0 - 3))", 0);
        testIntExpr("((1 + 3) >= (15 + 5)) || ((1 - 4) == (0 - 3))", 1);
        testIntExpr("1 || 0 || 0", 1);
        testIntExpr("1 || 0 && 0", 1);
        testIntExpr("1 && 0 || 0", 0);
        testIntExpr("0 && 0", 0);
        testIntExpr("1 && 1", 1);
        testIntExpr("0 && 1", 0);
        testIntExpr("1 && 0", 0);
        testIntExpr("0 || 0", 0);
        testIntExpr("1 || 1", 1);
        testIntExpr("0 || 1", 1);
        testIntExpr("1 || 0", 1);
        testIntExpr("1 > 0", 1);
        testIntExpr("1 > 1", 0);
        testIntExpr("1 > 2", 0);
        testIntExpr("1 >= 0", 1);
        testIntExpr("1 >= 1", 1);
        testIntExpr("1.0 >= 1.0", 1);
        testIntExpr("1.0 > 0.999", 1);
        testIntExpr("0.999 >= 0.999", 1);
        testIntExpr("1.0 >= 1.0001", 0);
        testIntExpr("1.0 >= 2", 0);
        testIntExpr("1 < 0", 0);
        testIntExpr("1 < 1", 0);
        testIntExpr("1 < 2", 1);
        testIntExpr("1 <= 0", 0);
        testIntExpr("1 <= 1", 1);
        testIntExpr("1 <= 2", 1);
        testIntExpr("1 <= 0.0", 0);
        testIntExpr("1 <= 1.0000000000001", 1);
        testIntExpr("1 <= 2.0", 1);
        testIntExpr("1 <= 0.999", 0);
        testIntExpr("0.999 <= 0.999", 1);
        testIntExpr("1 <= 1.0001", 1);
        testIntExpr("1 == 1", 1);
        testIntExpr("1 == 2", 0);
        testIntExpr("0 == 0", 1);
        testIntExpr("1.123 == 1.123", 1);
        testIntExpr("1.123 == 1.12300000001", 1);
        testIntExpr("1.123 == 1.1234", 0);
        testIntExpr("1 == 1.0", 1);
        testIntExpr("1 == 1.00000000001", 1);

        // Ternary
        testIntExpr("1 ? 2 : 3", 2);
        testIntExpr("0 ? 2 : 3", 3);
        testIntExpr("1 ? 2 : 0", 2);
        testIntExpr("0 ? 2 : 0", 0);
        testIntExpr("1 + 0 ? (1 + 1) : (2 + 1)", 2);
        testIntExpr("1 - 1 ? (1 + 1) : (2 + 1)", 3);
        testIntExpr("(1 - 1) ? (4 / 4) : (3 - 4)", -1);
        testIntExpr("(1 + 1) ? (8 / 4) : (3 - 4)", 2);
        testIntExpr("1 ? 1 ? 2 : 3 : 4", 2);
        testIntExpr("1 ? 1 ? 3 : 2 : 4", 3);
        // This kind of breaks without parens but fuck you if you write ternary like this anyways, the base shit works
        testIntExpr("0 ? (1 ? 3 : 2) : 4", 4);
        testIntExpr("1 ? 0 ? 3 : 2 : 4", 2);


        // Strings
        testStringExpr("'a'", "a");
        testStringExpr("'a' + 'b'", "ab");
        testStringExpr("1 ? 'hello' : 'world'", "hello");
        testStringExpr("0 ? 'hello' : 'world'", "world");
        testStringExpr("1 ? 'hello' : 'world' + 'foo'", "hello");
        testStringExpr("0 ? 'hello' : 'world' + 'foo'", "worldfoo");
        testStringExpr("(1 ? 'hello' : 'world') + 'foo'", "hellofoo");
        testStringExpr("(0 ? 'hello' : 'world') + 'foo'", "worldfoo");
        testIntExpr("'hello' == 'world'", 0);
        testIntExpr("'hello' != 'world'", 1);
        testIntExpr("'hello' == 'hello'", 1);
        testIntExpr("'hello' != 'hello'", 0);
        testIntExpr("'hello' == 'he' + 'llo'", 1);
        testStringExpr("('hello' + (1 > 5 ? ' world' : ' something else')) + ('abc' == ('a' + 'b' + 'c') ? 'foo' : 'bar')",
                "hello something elsefoo");

        // Color functions
        testStringExpr("red('a')", "<col=ff0000>a</col>");
        testStringExpr("green('a')", "<col=00ff00>a</col>");
        testStringExpr("blue('a')", "<col=0000ff>a</col>");
        testStringExpr("rgb(255, 255, 255, 'hi')", "<col=ffffff>hi</col>");
        testStringExpr("rgb(255, 0, 0, 'hi')", "<col=ff0000>hi</col>");
        testStringExpr("rgb(0, 255, 0, 'hi')", "<col=00ff00>hi</col>");
        testStringExpr("rgb(0, 0, 255, 'hi')", "<col=0000ff>hi</col>");
        testStringExpr("rgb(255, 255, 0, 'hi')", "<col=ffff00>hi</col>");
        testStringExpr("rgb(255, 255, 0, green(redStart() + blueStart()))", "<col=ffff00><col=00ff00><col=ff0000><col=0000ff></col></col>");

        // redStart(), greenStart(), blueStart(), yellowStart(), rgbStart(), colorEnd()
        testStringExpr("redStart()", "<col=ff0000>");
        testStringExpr("greenStart()", "<col=00ff00>");
        testStringExpr("blueStart()", "<col=0000ff>");
        testStringExpr("yellowStart()", "<col=ffff00>");
        testStringExpr("rgbStart(255, 255, 255)", "<col=ffffff>");
        testStringExpr("rgbStart(255, 0, 0)", "<col=ff0000>");
        testStringExpr("rgbStart(0, 255, 0)", "<col=00ff00>");
        testStringExpr("rgbStart(0, 0, 255)", "<col=0000ff>");
        testStringExpr("rgbStart(255, 255, 0) + 'hi' + colorEnd()", "<col=ffff00>hi</col>");

        // eval with math and stuff
//        testDoubleExpr("eval({(1 + 3) * (4.5 * 2)})", 36);


    }

    private void testIntExpr(String expression, int expected) {
        Tokenizer t = new Tokenizer("${" + expression + "}", 0);
        CompiledExpression compiled = Compiler.Compile(t.tokenize());
        CompiledExpression condensed = EvaluationUtil.EvaluateStatically(compiled, context);
        Token result = EvaluationUtil.EvaluateAtRuntime(condensed, context);

        assertNotSame(result.getType(), TokenType.STRING);
        assertEquals(expression, expected, result.getNumericValue().intValue());
    }

    private void testDoubleExpr(String expression, double expected) {
        Tokenizer t = new Tokenizer("<%" + expression + "%>", 0);
        CompiledExpression compiled = Compiler.Compile(t.tokenize());
        CompiledExpression condensed = EvaluationUtil.EvaluateStatically(compiled, context);
        Token result = EvaluationUtil.EvaluateAtRuntime(condensed, context);

        assertNotSame(result.getType(), TokenType.STRING);
        assertEquals(expression, expected, result.getNumericValue().doubleValue(), EPSILON);
    }

    private void testStringExpr(String expression, String expected) {
        Tokenizer t = new Tokenizer("<%" + expression + "%>", 0);
        CompiledExpression compiled = Compiler.Compile(t.tokenize());
        CompiledExpression condensed = EvaluationUtil.EvaluateStatically(compiled, context);
        Token result = EvaluationUtil.EvaluateAtRuntime(condensed, context);

        assertEquals(result.getType(), TokenType.STRING);
        assertEquals(expression, expected, result.getValue());
    }
}
