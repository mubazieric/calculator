package com.iwat.scientific;

/** Exact rules for expanded polynomials up to degree eight; not a general CAS. */
public final class Polynomial {
    private static final int MAX_DEGREE=8;
    private final String source;
    private int position;
    private Polynomial(String source) { this.source=source.replace('×','*').replace('−','-'); }

    public static String differentiate(String expression) { return render(derivative(parse(expression)),false); }
    public static String integrate(String expression) { return render(antiderivative(parse(expression)),true); }

    private static double[] parse(String expression) {
        Polynomial parser=new Polynomial(expression); double[] value=parser.sum(); parser.spaces();
        if(parser.position!=parser.source.length()) throw parser.error("Unsupported polynomial syntax");
        return value;
    }
    private static double[] derivative(double[] p) {
        double[] out=new double[MAX_DEGREE+1]; for(int i=1;i<p.length;i++) out[i-1]=p[i]*i; return out;
    }
    private static double[] antiderivative(double[] p) {
        double[] out=new double[MAX_DEGREE+2]; for(int i=0;i<p.length;i++) out[i+1]=p[i]/(i+1); return out;
    }
    private static double[] add(double[] a,double[] b,int sign) {
        double[] out=new double[MAX_DEGREE+1]; for(int i=0;i<out.length;i++) out[i]=AdvancedMath.finite(a[i]+sign*b[i]); return out;
    }
    private static double[] product(double[] a,double[] b) {
        double[] out=new double[MAX_DEGREE+1];
        for(int i=0;i<a.length;i++) for(int j=0;j<b.length;j++) if(a[i]!=0 && b[j]!=0) {
            if(i+j>MAX_DEGREE) throw new IllegalArgumentException("Polynomial degree cannot exceed 8");
            out[i+j]=AdvancedMath.finite(out[i+j]+a[i]*b[j]);
        }
        return out;
    }
    private static double[] constant(double value) { double[] p=new double[MAX_DEGREE+1]; p[0]=value; return p; }
    private double[] sum() {
        double[] a=term(); while(true) {
            if(eat('+')) a=add(a,term(),1);
            else if(eat('-')) a=add(a,term(),-1);
            else return a;
        }
    }
    private double[] term() {
        double[] a=signed(); while(true) {
            if(eat('*')) a=product(a,signed());
            else if(eat('/')) { double[] divisor=signed(); for(int i=1;i<divisor.length;i++) if(divisor[i]!=0) throw error("Division by x is not a polynomial");
                if(divisor[0]==0) throw error("Division by zero"); a=product(a,constant(1/divisor[0])); }
            else if(peek('x') || peek('(')) a=product(a,signed());
            else return a;
        }
    }
    private double[] signed() {
        if(eat('+')) return signed();
        if(eat('-')) return product(constant(-1),signed());
        return power();
    }
    private double[] power() {
        double[] a=atom(); if(eat('^')) {
            spaces(); int start=position; while(position<source.length() && Character.isDigit(source.charAt(position))) position++;
            if(start==position) throw error("Use a nonnegative integer power");
            int n; try { n=Integer.parseInt(source.substring(start,position)); } catch(NumberFormatException e) { throw error("Power is too large"); }
            if(n>MAX_DEGREE) throw error("Power cannot exceed 8");
            double[] result=constant(1); for(int i=0;i<n;i++) result=product(result,a); return result;
        }
        return a;
    }
    private double[] atom() {
        if(eat('(')) { double[] result=sum(); if(!eat(')')) throw error("Missing )"); return result; }
        if(eat('x')) { double[] result=constant(0); result[1]=1; return result; }
        if(eat('π')) return constant(Math.PI);
        spaces(); int start=position;
        while(position<source.length() && (Character.isDigit(source.charAt(position)) || source.charAt(position)=='.')) position++;
        if(start==position) throw error("Expected a number or x");
        try { return constant(AdvancedMath.finite(Double.parseDouble(source.substring(start,position)))); }
        catch(NumberFormatException e) { throw error("Invalid number"); }
    }
    private boolean peek(char value) { spaces(); return position<source.length() && source.charAt(position)==value; }
    private boolean eat(char value) { if(peek(value)) { position++; return true; } return false; }
    private void spaces() { while(position<source.length() && Character.isWhitespace(source.charAt(position))) position++; }
    private IllegalArgumentException error(String message) { return new IllegalArgumentException(message+" near character "+(position+1)); }
    private static String render(double[] p,boolean withConstant) {
        StringBuilder result=new StringBuilder();
        for(int power=p.length-1;power>=0;power--) {
            double coefficient=p[power]; if(Math.abs(coefficient)<1e-12) continue;
            if(result.length()>0) result.append(coefficient<0?" - ":" + "); else if(coefficient<0) result.append('-');
            double magnitude=Math.abs(coefficient);
            if(power==0 || Math.abs(magnitude-1)>1e-12) result.append(Expression.format(magnitude));
            if(power>0) result.append("x");
            if(power>1) result.append('^').append(power);
        }
        if(result.length()==0) result.append('0');
        if(withConstant) result.append(" + C");
        return result.toString();
    }
}
