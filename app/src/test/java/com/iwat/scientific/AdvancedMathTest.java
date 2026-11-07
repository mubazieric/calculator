package com.iwat.scientific;

import org.junit.Test;
import static org.junit.Assert.*;

public class AdvancedMathTest {
    @Test public void calculus() {
        assertEquals(6, AdvancedMath.derivative("x^2",3,true),1e-7);
        assertEquals(1.0/3,AdvancedMath.integral("x^2",0,1,true),1e-7);
        assertEquals(2,AdvancedMath.solve("x^2-4",0,3,true),1e-9);
        assertThrows(IllegalArgumentException.class,()->AdvancedMath.solve("x^2+1",-2,2,true));
    }
    @Test public void statisticsAndLinearAlgebra() {
        assertArrayEquals(new double[]{10,2.5,2.5,Math.sqrt(1.25)},AdvancedMath.statistics(new double[]{1,2,3,4}),1e-12);
        assertEquals(-2,AdvancedMath.determinant(1,2,3,4),1e-12);
        assertArrayEquals(new double[]{-2,1,1.5,-.5},AdvancedMath.inverse(1,2,3,4),1e-12);
        assertEquals(32,AdvancedMath.dot(new double[]{1,2,3},new double[]{4,5,6}),1e-12);
        assertThrows(IllegalArgumentException.class,()->AdvancedMath.inverse(1,2,2,4));
    }
    @Test public void numberTools() {
        assertEquals(6,AdvancedMath.gcd(54,24));
        assertEquals(216,AdvancedMath.lcm(54,24));
        assertEquals("3/4",AdvancedMath.fraction(.75));
        assertEquals("-1/3",AdvancedMath.fraction(-1.0/3));
    }
}
