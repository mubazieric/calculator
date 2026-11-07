package com.iwat.scientific;

import java.util.Arrays;

/** Offline numerical tools. Inputs are expressions parsed by Expression, not executable code. */
public final class AdvancedMath {
    private AdvancedMath() { }

    private static double at(String formula, double x, boolean degrees) {
        return new Expression(formula, degrees, 0, x).evaluate();
    }

    public static double derivative(String formula, double x, boolean degrees) {
        double h = Math.max(1e-5, Math.abs(x) * 1e-5);
        return finite((at(formula, x - 2*h, degrees) - 8*at(formula, x-h, degrees)
                + 8*at(formula, x+h, degrees) - at(formula, x+2*h, degrees)) / (12*h));
    }

    public static double integral(String formula, double a, double b, boolean degrees) {
        int segments = 2000; double h = (b-a)/segments;
        double sum = at(formula,a,degrees) + at(formula,b,degrees);
        for(int i=1;i<segments;i++) sum += (i%2==0?2:4)*at(formula,a+i*h,degrees);
        return finite(sum*h/3);
    }

    /** Bisection is deliberately bracketed to avoid returning a false root. */
    public static double solve(String formula, double a, double b, boolean degrees) {
        if (!(a < b)) throw new IllegalArgumentException("The lower bound must be less than the upper bound");
        double left=at(formula,a,degrees), right=at(formula,b,degrees);
        if(left==0) return a;
        if(right==0) return b;
        if(Math.signum(left)==Math.signum(right)) throw new IllegalArgumentException("Choose bounds where the function changes sign");
        for(int i=0;i<160;i++) {
            double mid=a+(b-a)/2, value=at(formula,mid,degrees);
            if (Math.abs(value)<1e-12 || Math.abs(b-a)<1e-12*Math.max(1,Math.abs(mid))) return mid;
            if(Math.signum(value)==Math.signum(left)) { a=mid; left=value; } else { b=mid; right=value; }
        }
        return finite(a+(b-a)/2);
    }

    public static double[] statistics(double[] values) {
        if(values.length==0) throw new IllegalArgumentException("Enter at least one value");
        double[] sorted=values.clone(); Arrays.sort(sorted);
        double sum=0; for(double v:values) sum+=finite(v);
        double mean=sum/values.length, variance=0;
        for(double v:values) variance+=(v-mean)*(v-mean);
        return new double[]{finite(sum),finite(mean),finite(sorted.length%2==1?sorted[sorted.length/2]
                :(sorted[sorted.length/2-1]+sorted[sorted.length/2])/2),finite(Math.sqrt(variance/values.length))};
    }

    public static double determinant(double a,double b,double c,double d) { return finite(a*d-b*c); }
    public static double[] inverse(double a,double b,double c,double d) {
        double det=determinant(a,b,c,d);
        if(Math.abs(det)<1e-14) throw new IllegalArgumentException("Matrix has no inverse");
        return new double[]{d/det,-b/det,-c/det,a/det};
    }
    public static double dot(double[] a,double[] b) {
        if(a.length!=b.length || a.length==0) throw new IllegalArgumentException("Vectors need equal dimensions");
        double sum=0; for(int i=0;i<a.length;i++) sum+=a[i]*b[i]; return finite(sum);
    }
    public static long gcd(long a,long b) {
        if(a==Long.MIN_VALUE || b==Long.MIN_VALUE) throw new IllegalArgumentException("Number is too large");
        a=Math.abs(a); b=Math.abs(b); while(b!=0) { long t=a%b; a=b; b=t; } return a;
    }
    public static long lcm(long a,long b) {
        if(a==0 || b==0) return 0;
        return Math.abs(Math.multiplyExact(a/gcd(a,b),b));
    }
    public static String fraction(double value) {
        if(!Double.isFinite(value)) throw new IllegalArgumentException("Undefined result");
        long bestN=0,bestD=1; double bestError=Double.MAX_VALUE;
        for(int d=1;d<=10000;d++) {
            if(Math.abs(value)>Long.MAX_VALUE/(double)d) break;
            long n=Math.round(value*d); double error=Math.abs(value-(double)n/d);
            if(error<bestError) { bestN=n; bestD=d; bestError=error; }
            if(error<1e-10) break;
        }
        if(bestError>1e-7) throw new IllegalArgumentException("No simple fraction found");
        return bestD==1?Long.toString(bestN):bestN+"/"+bestD;
    }
    public static double finite(double value) {
        if(!Double.isFinite(value)) throw new IllegalArgumentException("Undefined or overflowing result");
        return value;
    }
}
