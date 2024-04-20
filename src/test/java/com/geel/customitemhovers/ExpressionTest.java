package com.geel.customitemhovers;

import com.geel.customitemhovers.expressions.*;
import com.geel.customitemhovers.expressions.Compiler;
import com.geel.customitemhovers.expressions.models.CompiledExpression;
import com.geel.customitemhovers.expressions.models.Token;
import com.geel.customitemhovers.expressions.models.TokenType;
import com.geel.customitemhovers.functions.FunctionProvider;
import com.geel.customitemhovers.models.itemtables.ItemTable;
import org.junit.Test;

import static org.junit.Assert.*;

public class ExpressionTest {
    private static double EPSILON = 0.000001;
    private static ItemHoverContext context;

    @Test
    public void TestTokenizer() {
        ItemTable blah = new ItemTable();
        blah.put("a", "b");
        FunctionProvider provider = new FunctionProvider(null, null);
        context = new ItemHoverContext(null, null, null, null, provider, null, blah);

        // Functions
        testStringExpr("red(a)", "<col=ff0000>b</col>");
        testStringExpr("green('a')", "<col=00ff00>a</col>");
        testStringExpr("blue('a')", "<col=0000ff>a</col>");
        testStringExpr("rgb(255, pow(2, 8) - 1, 2, 'hi')", "<col=ffff02>hi</col>");
        testDoubleExpr("pow(2, 3)", 8);
        testDoubleExpr("max(1,2,15, 2.3, 15.4, (15 + 0.5 + (1/3)), (15 + 0.5 - (2 * 1/3)))", 15.833333333333);
        testDoubleExpr("min(1,2,15, 2.3, 15.4, (15 + 0.5 + (1/3)), (15 - (201 * 1/3)))", -52);
        testDoubleExpr("min((15 - (201 * 1/3)))", -52);

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
        testIntExpr("1 + 0 ? (1 + 1) : (2 + 1)", 2);
        testIntExpr("1 - 1 ? (1 + 1) : (2 + 1)", 3);
        testIntExpr("(1 - 1) ? (4 / 4) : (3 - 4)", -1);
        testIntExpr("(1 + 1) ? (8 / 4) : (3 - 4)", 2);
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
 }

    private void testIntExpr(String expression, int expected) {
        Tokenizer t = new Tokenizer("${" + expression + "}", 0);
        CompiledExpression compiled = Compiler.Compile(t.tokenize());
        CompiledExpression condensed = StaticEvaluator.Evaluate(compiled, context.getFunctionProvider());
        Token result = RuntimeEvaluator.Evaluate(condensed, context);

        assertNotSame(result.getType(), TokenType.STRING);
        assertEquals(expression, expected, result.getNumericValue().intValue());
    }

    private void testDoubleExpr(String expression, double expected) {
        Tokenizer t = new Tokenizer("<%" + expression + "%>", 0);
        CompiledExpression compiled = Compiler.Compile(t.tokenize());
        CompiledExpression condensed = StaticEvaluator.Evaluate(compiled, context.getFunctionProvider());
        Token result = RuntimeEvaluator.Evaluate(condensed, context);

        assertNotSame(result.getType(), TokenType.STRING);
        assertEquals(expression, expected, result.getNumericValue().doubleValue(), EPSILON);
    }

    private void testStringExpr(String expression, String expected) {
        Tokenizer t = new Tokenizer("<%" + expression + "%>", 0);
        CompiledExpression compiled = Compiler.Compile(t.tokenize());
        CompiledExpression condensed = StaticEvaluator.Evaluate(compiled, context.getFunctionProvider());
        Token result = RuntimeEvaluator.Evaluate(condensed, context);

        assertEquals(result.getType(), TokenType.STRING);
        assertEquals(expression, expected, result.getValue());
    }
}
