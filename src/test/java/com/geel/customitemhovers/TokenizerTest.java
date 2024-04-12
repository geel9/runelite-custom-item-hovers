package com.geel.customitemhovers;

import com.geel.customitemhovers.expressions.ExpressionEvaluator;
import com.geel.customitemhovers.expressions.ExpressionParser;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TokenizerTest {
    private static double EPSILON = 0.000001;

    @Test
    public void TestTokenizer() {
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
    }

    private void testIntExpr(String expression, int expected) {
        assertEquals(expression, expected, ExpressionEvaluator.Evaluate(ExpressionParser.Parse(expression), null).intValue());
    }

    private void testDoubleExpr(String expression, double expected) {
        assertEquals(expression, expected, ExpressionEvaluator.Evaluate(ExpressionParser.Parse(expression), null).doubleValue(), EPSILON);
    }
}
