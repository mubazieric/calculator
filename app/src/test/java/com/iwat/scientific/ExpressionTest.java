package com.iwat.scientific;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertNotEquals;

public class ExpressionTest {
    private void check(String expression, boolean degrees, double expected) {
        double actual = new Expression(expression, degrees, 4).evaluate();
        assertEquals(expression, expected, actual, 1e-9 * Math.max(1, Math.abs(expected)));
    }

    @Test public void arithmeticAndPrecedence() {
        check("2+3*4", true, 14);
        check("(2+3)^2", true, 25);
        check("2^3^2", true, 512);
        check("-2^2", true, -4);
        check("2π", true, 2*Math.PI);
        check("2(3+4)", true, 14);
        check("(1+2)(3+4)", true, 21);
        check("Ans^2", true, 16);
        check("200×10%", true, 20);
        check("50%", true, .5);
    }

    @Test public void scientificFunctions() {
        check("sin(30)+√(16)", true, 4.5);
        check("sin(π/2)", false, 1);
        check("sin(180)+cos(90)", true, 0);
        check("asin(1)", true, 90);
        check("log(1000)+ln(e)", true, 4);
        check("cbrt(-8)", true, -2);
        check("5 nCr 2", true, 10);
        check("5 nPr 2", true, 20);
        check("5!", true, 120);
        check("1E-10", true, 1e-10);
        check("floor(2.9)+ceil(1.1)",true,4);
        check("mod(10,3)",true,1);
        check("cot(45)",true,1);
        assertNotEquals("0", Expression.format(1e-15));
    }

    @Test public void invalidExpressionsAreRejected() {
        for (String expression : new String[]{"√(-1)","4/0","5 nCr 6","2+","tan(90)"}) {
            assertThrows(expression, IllegalArgumentException.class,
                () -> new Expression(expression, true, 0).evaluate());
        }
    }
}
