package com.iwat.scientific;

import org.junit.Test;
import static org.junit.Assert.*;

public class PolynomialTest {
    @Test public void derivatives() {
        assertEquals("6x - 4",Polynomial.differentiate("3x^2-4x+1"));
        assertEquals("2x + 2",Polynomial.differentiate("(x+1)^2"));
        assertEquals("-2x",Polynomial.differentiate("-x^2"));
    }
    @Test public void antiderivatives() {
        assertEquals("x^3 + 2x + C",Polynomial.integrate("3x^2+2"));
        assertEquals("0 + C",Polynomial.integrate("0"));
    }
    @Test public void unsupportedExpressionsAreRejected() {
        assertThrows(IllegalArgumentException.class,()->Polynomial.integrate("1/x"));
        assertThrows(IllegalArgumentException.class,()->Polynomial.differentiate("x^9"));
        assertThrows(IllegalArgumentException.class,()->Polynomial.differentiate("sin(x)"));
    }
}
