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
        assertThrows(IllegalArgumentException.class,()->AdvancedMath.fraction(1e-15));
    }
    @Test public void extendedMatricesAndVectors() {
        double[][] matrix={{2,0,1},{1,3,0},{0,1,2}};
        assertEquals(13,AdvancedMath.matrixDeterminant(matrix),1e-12);
        double[][] inverse=AdvancedMath.matrixInverse(matrix);
        double[][] identity=AdvancedMath.matrixMultiply(matrix,inverse);
        for(int i=0;i<3;i++) for(int j=0;j<3;j++) assertEquals(i==j?1:0,identity[i][j],1e-10);
        assertArrayEquals(new double[]{1,2,3},AdvancedMath.linearSystem(matrix,new double[]{5,7,8}),1e-10);
        assertArrayEquals(new double[]{0,0,1},AdvancedMath.cross(new double[]{1,0,0},new double[]{0,1,0}),1e-12);
        assertEquals(5,AdvancedMath.magnitude(new double[]{3,4}),1e-12);
        assertThrows(IllegalArgumentException.class,()->AdvancedMath.matrixInverse(new double[][]{{1,2},{2,4}}));
        double[][] six=new double[6][6]; for(int i=0;i<6;i++) six[i][i]=i+1;
        assertEquals(720,AdvancedMath.matrixDeterminant(six),1e-9);
        assertEquals(1,AdvancedMath.matrixMultiply(six,AdvancedMath.matrixInverse(six))[5][5],1e-10);
        assertArrayEquals(new double[]{2,3},AdvancedMath.linearSystem(new double[][]{{1e-13,0},{0,1e-13}},new double[]{2e-13,3e-13}),1e-10);
    }
    @Test public void tablesLimitsAndEquations() {
        assertEquals(1,AdvancedMath.limit("sin(x)/x",0,false),1e-4);
        assertThrows(IllegalArgumentException.class,()->AdvancedMath.limit("1/x",0,false));
        double[][] rows=AdvancedMath.table("x^2",0,2,1,true);
        assertArrayEquals(new double[]{2,4},rows[2],1e-12);
        assertThrows(IllegalArgumentException.class,()->AdvancedMath.table("x",0,1000,1,true));
        assertTrue(AdvancedMath.quadratic(1,0,1).contains("i"));
        assertTrue(AdvancedMath.quadratic(1,-3,2).contains("x₁ = 2"));
        assertTrue(AdvancedMath.quadratic(1,1e12,1).contains("-1E-12"));
    }
    @Test public void distributions() {
        assertEquals(.5,AdvancedMath.normalCdf(0,0,1),1e-6);
        assertEquals(1/Math.sqrt(2*Math.PI),AdvancedMath.normalPdf(0,0,1),1e-12);
        assertEquals(.375,AdvancedMath.binomial(4,2,.5),1e-12);
        assertEquals(Math.exp(-2)*2,AdvancedMath.poisson(1,2),1e-12);
        assertThrows(IllegalArgumentException.class,()->AdvancedMath.normalCdf(0,0,0));
        assertThrows(IllegalArgumentException.class,()->AdvancedMath.binomialCdf(-1,-1,.5));
        assertThrows(IllegalArgumentException.class,()->AdvancedMath.poissonCdf(-1,-1));
        assertEquals(.6875,AdvancedMath.binomialCdf(4,2,.5),1e-12);
        assertEquals(Math.exp(-2)*5,AdvancedMath.poissonCdf(2,2),1e-12);
        assertEquals(36.0/70,AdvancedMath.hypergeometric(8,4,4,2),1e-12);
    }
}
