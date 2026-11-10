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
    public static double magnitude(double[] vector) { return Math.sqrt(dot(vector,vector)); }
    public static double[] cross(double[] a,double[] b) {
        if(a.length!=3 || b.length!=3) throw new IllegalArgumentException("Cross product needs two 3D vectors");
        return new double[]{finite(a[1]*b[2]-a[2]*b[1]),finite(a[2]*b[0]-a[0]*b[2]),finite(a[0]*b[1]-a[1]*b[0])};
    }

    private static void square(double[][] matrix) {
        if(matrix==null || matrix.length<2 || matrix.length>6) throw new IllegalArgumentException("Use a 2×2 through 6×6 matrix");
        for(double[] row:matrix) {
            if(row==null || row.length!=matrix.length) throw new IllegalArgumentException("Matrix must be square");
            for(double value:row) finite(value);
        }
    }
    public static double matrixDeterminant(double[][] matrix) {
        square(matrix);
        int n=matrix.length; double[][] a=new double[n][n];
        for(int i=0;i<n;i++) System.arraycopy(matrix[i],0,a[i],0,n);
        double det=1;
        for(int col=0;col<n;col++) {
            int pivot=col; for(int row=col+1;row<n;row++) if(Math.abs(a[row][col])>Math.abs(a[pivot][col])) pivot=row;
            if(a[pivot][col]==0) return 0;
            if(pivot!=col) { double[] tmp=a[col]; a[col]=a[pivot]; a[pivot]=tmp; det=-det; }
            double diagonal=a[col][col]; det*=diagonal;
            for(int row=col+1;row<n;row++) {
                double factor=a[row][col]/diagonal;
                for(int c=col+1;c<n;c++) a[row][c]-=factor*a[col][c];
            }
        }
        return finite(det);
    }
    /** Partial-pivot Gauss-Jordan elimination for small square systems. */
    public static double[] linearSystem(double[][] matrix,double[] rhs) {
        square(matrix); int n=matrix.length;
        if(rhs==null || rhs.length!=n) throw new IllegalArgumentException("Enter one result per equation");
        double[][] a=new double[n][n+1];
        double scale=0;
        for(int r=0;r<n;r++) {
            System.arraycopy(matrix[r],0,a[r],0,n); a[r][n]=finite(rhs[r]);
            for(int c=0;c<n;c++) scale=Math.max(scale,Math.abs(a[r][c]));
        }
        if(scale==0) throw new IllegalArgumentException("No unique solution (singular system)");
        for(int col=0;col<n;col++) {
            int pivot=col; for(int r=col+1;r<n;r++) if(Math.abs(a[r][col])>Math.abs(a[pivot][col])) pivot=r;
            if(Math.abs(a[pivot][col])<=scale*1e-14) throw new IllegalArgumentException("No unique solution (singular system)");
            double[] tmp=a[pivot]; a[pivot]=a[col]; a[col]=tmp;
            double pivotValue=a[col][col]; for(int c=col;c<=n;c++) a[col][c]/=pivotValue;
            for(int r=0;r<n;r++) if(r!=col) {
                double factor=a[r][col]; for(int c=col;c<=n;c++) a[r][c]-=factor*a[col][c];
            }
        }
        double[] solution=new double[n]; for(int i=0;i<n;i++) solution[i]=finite(a[i][n]); return solution;
    }
    public static double[][] matrixInverse(double[][] matrix) {
        square(matrix); int n=matrix.length; double[][] inverse=new double[n][n];
        for(int c=0;c<n;c++) {
            double[] unit=new double[n]; unit[c]=1;
            double[] column=linearSystem(matrix,unit);
            for(int r=0;r<n;r++) inverse[r][c]=column[r];
        }
        return inverse;
    }
    public static double[][] matrixMultiply(double[][] a,double[][] b) {
        square(a); square(b); if(a.length!=b.length) throw new IllegalArgumentException("Matrix sizes must match");
        int n=a.length; double[][] result=new double[n][n];
        for(int i=0;i<n;i++) for(int j=0;j<n;j++) for(int k=0;k<n;k++) result[i][j]+=a[i][k]*b[k][j];
        for(double[] row:result) for(double x:row) finite(x);
        return result;
    }
    public static double limit(String formula,double x,boolean degrees) {
        double lastLeft=0,lastRight=0,step=Math.max(0.01,Math.abs(x)*.01);
        for(int i=0;i<10;i++) {
            step/=2;
            double left=at(formula,x-step,degrees),right=at(formula,x+step,degrees);
            if(i==9) {
                double tolerance=1e-3*Math.max(1,Math.max(Math.abs(left),Math.abs(right)));
                if(Math.abs(left-right)>tolerance || Math.abs(left-lastLeft)>tolerance || Math.abs(right-lastRight)>tolerance)
                    throw new IllegalArgumentException("Limit does not converge from both sides");
                return finite((left+right)/2);
            }
            lastLeft=left; lastRight=right;
        }
        throw new IllegalArgumentException("Limit could not be evaluated");
    }
    public static double[][] table(String formula,double start,double end,double step,boolean degrees) {
        if(!(step>0) || end<start || (end-start)/step>99.0000001) throw new IllegalArgumentException("Use a positive step and at most 100 rows");
        int count=(int)Math.floor((end-start)/step+1e-9)+1;
        double[][] rows=new double[count][2];
        for(int i=0;i<count;i++) { rows[i][0]=finite(start+i*step); rows[i][1]=at(formula,rows[i][0],degrees); }
        return rows;
    }
    public static String quadratic(double a,double b,double c) {
        if(a==0) {
            if(b==0) throw new IllegalArgumentException("No unique root");
            return "x = " + Expression.format(finite(-c/b));
        }
        double d=finite(b*b-4*a*c);
        if(d>=0) {
            double root=Math.sqrt(d);
            double stable=-.5*(b+Math.copySign(root,b));
            double first=stable==0?0:stable/a;
            double second=stable==0?0:c/stable;
            return "x₁ = " + Expression.format(finite(first)) + "\nx₂ = " + Expression.format(finite(second));
        }
        double real=finite(-b/(2*a)),imag=finite(Math.sqrt(-d)/Math.abs(2*a));
        return "x₁ = " + Expression.format(real) + " + " + Expression.format(imag) + "i\nx₂ = " + Expression.format(real) + " − " + Expression.format(imag) + "i";
    }
    public static double normalPdf(double x,double mean,double sigma) {
        if(!(sigma>0)) throw new IllegalArgumentException("Standard deviation must be positive");
        double z=(x-mean)/sigma; return finite(Math.exp(-z*z/2)/(sigma*Math.sqrt(2*Math.PI)));
    }
    public static double normalCdf(double x,double mean,double sigma) {
        if(!(sigma>0)) throw new IllegalArgumentException("Standard deviation must be positive");
        double z=(x-mean)/sigma, absolute=Math.abs(z), t=1/(1+0.2316419*absolute);
        double density=Math.exp(-absolute*absolute/2)/Math.sqrt(2*Math.PI);
        double tail=density*t*(0.319381530+t*(-0.356563782+t*(1.781477937+t*(-1.821255978+t*1.330274429))));
        return Math.max(0,Math.min(1,z>=0?1-tail:tail));
    }
    public static double binomial(int n,int k,double p) {
        if(n<0 || n>1000 || k<0 || k>n || p<0 || p>1) throw new IllegalArgumentException("Use 0 ≤ k ≤ n ≤ 1000 and 0 ≤ p ≤ 1");
        if(p==0) return k==0?1:0; if(p==1) return k==n?1:0;
        double log=0; for(int i=1;i<=k;i++) log+=Math.log(n-k+i)-Math.log(i);
        return finite(Math.exp(log+k*Math.log(p)+(n-k)*Math.log1p(-p)));
    }
    public static double poisson(int k,double lambda) {
        if(k<0 || k>10000 || lambda<0 || !Double.isFinite(lambda)) throw new IllegalArgumentException("Use k ≥ 0 and λ ≥ 0");
        if(lambda==0) return k==0?1:0;
        double log=-lambda+k*Math.log(lambda); for(int i=2;i<=k;i++) log-=Math.log(i);
        return finite(Math.exp(log));
    }
    public static double binomialCdf(int n,int k,double p) {
        if(n<0 || n>1000 || p<0 || p>1 || !Double.isFinite(p)) throw new IllegalArgumentException("Use 0 ≤ n ≤ 1000 and 0 ≤ p ≤ 1");
        if(k<0) return 0;
        if(k>=n) { binomial(n,n,p); return 1; }
        double sum=0; for(int i=0;i<=k;i++) sum+=binomial(n,i,p);
        return Math.min(1,finite(sum));
    }
    public static double poissonCdf(int k,double lambda) {
        if(lambda<0 || !Double.isFinite(lambda)) throw new IllegalArgumentException("Use λ ≥ 0");
        if(k<0) return 0;
        if(k>1000) throw new IllegalArgumentException("Use k ≤ 1000");
        double sum=0; for(int i=0;i<=k;i++) sum+=poisson(i,lambda);
        return Math.min(1,finite(sum));
    }
    private static double logChoose(int n,int k) {
        if(k<0 || k>n) return Double.NEGATIVE_INFINITY;
        k=Math.min(k,n-k); double sum=0;
        for(int i=1;i<=k;i++) sum+=Math.log(n-k+i)-Math.log(i);
        return sum;
    }
    public static double hypergeometric(int population,int successes,int draws,int observed) {
        if(population<=0 || population>100000 || successes<0 || successes>population || draws<0 || draws>population || observed<0 || observed>draws)
            throw new IllegalArgumentException("Use 0 ≤ successes, draws ≤ population ≤ 100000");
        return finite(Math.exp(logChoose(successes,observed)+logChoose(population-successes,draws-observed)-logChoose(population,draws)));
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
        if(value!=0 && Math.abs(value)<1e-7) throw new IllegalArgumentException("No simple fraction found");
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
