package com.iwat.scientific;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Recursive descent arithmetic engine. No eval(), scripts or network execution. */
public final class Expression {
    private final String input;
    private final boolean degrees;
    private final double answer;
    private final double variable;
    private int position;

    public Expression(String input, boolean degrees, double answer) {
        this(input, degrees, answer, Double.NaN);
    }

    public Expression(String input, boolean degrees, double answer, double variable) {
        this.input = input.replace('×', '*').replace('÷', '/').replace('−', '-')
                .replace('π', 'p').replace('√', 'r').replace("Ans", "a")
                .replace("²", "^2").replace("³", "^3").replace("⁻", "-").replace("⁺", "+");
        this.degrees = degrees;
        this.answer = answer;
        this.variable = variable;
    }

    public double evaluate() {
        position = 0;
        double result = sum();
        spaces();
        if (position != input.length()) throw error("Unexpected character");
        if (!Double.isFinite(result)) throw error("Undefined result");
        return result;
    }

    private double sum() {
        double x = product();
        while (true) {
            if (eat('+')) x += product();
            else if (eat('-')) x -= product();
            else return x;
        }
    }

    private double product() {
        double x = signed();
        while (true) {
            if (eat('*')) x *= signed();
            else if (eat('/')) x /= signed();
            else if (word("nCr")) x = combinations(x, signed());
            else if (word("nPr")) x = permutations(x, signed());
            else if (nextAtom()) x *= signed(); // 2π, 2(3+4), (1+2)(3+4)
            else return x;
        }
    }

    private double signed() {
        if (eat('+')) return signed();
        if (eat('-')) return -signed();
        return power();
    }

    private double power() {
        double x = atom();
        while (true) {
            if (eat('!')) x = factorial(x);
            else if (eat('%')) x /= 100;
            else break;
        }
        if (eat('^')) x = Math.pow(x, signed()); // right associative; -2^2 = -4
        return x;
    }

    private double atom() {
        if (eat('(')) {
            double x = sum();
            if (!eat(')')) throw error("Missing )");
            return x;
        }
        spaces();
        int start = position;
        while (position < input.length() && (Character.isDigit(input.charAt(position)) || input.charAt(position) == '.')) position++;
        if (start != position) {
            if (position < input.length() && (input.charAt(position) == 'E' || input.charAt(position) == 'e')
                    && position + 1 < input.length() && (Character.isDigit(input.charAt(position + 1)) || input.charAt(position + 1) == '+' || input.charAt(position + 1) == '-')) {
                int exponentStart = position++;
                if (position < input.length() && (input.charAt(position) == '+' || input.charAt(position) == '-')) position++;
                int digits = position;
                while (position < input.length() && Character.isDigit(input.charAt(position))) position++;
                if (position == digits) position = exponentStart;
            }
            try { return Double.parseDouble(input.substring(start, position)); }
            catch (NumberFormatException e) { throw error("Invalid number"); }
        }
        if (word("asin")) return fromAngle(Math.asin(argument()));
        if (word("acos")) return fromAngle(Math.acos(argument()));
        if (word("atan")) return fromAngle(Math.atan(argument()));
        if (word("acot")) return fromAngle(Math.atan2(1,argument()));
        if (word("sinh")) return Math.sinh(argument());
        if (word("cosh")) return Math.cosh(argument());
        if (word("tanh")) return Math.tanh(argument());
        if (word("sin")) { double x = toAngle(argument()); return cleanTrig(Math.sin(x), x); }
        if (word("cos")) { double x = toAngle(argument()); return cleanTrig(Math.cos(x), x); }
        if (word("tan")) {
            double x = toAngle(argument());
            if (Math.abs(Math.cos(x)) < 1e-12) throw new IllegalArgumentException("Tangent is undefined at this angle");
            return cleanTrig(Math.tan(x), x);
        }
        if (word("cot")) {
            double x=toAngle(argument());
            if(Math.abs(Math.sin(x))<1e-12) throw new IllegalArgumentException("Cotangent is undefined at this angle");
            return cleanTrig(1/Math.tan(x),x);
        }
        if (word("log")) return Math.log10(argument());
        if (word("ln")) return Math.log(argument());
        if (word("abs")) return Math.abs(argument());
        if (word("floor")) return Math.floor(argument());
        if (word("ceil")) return Math.ceil(argument());
        if (word("mod")) {
            if(!eat('(')) throw error("Expected (");
            double first=sum(); if(!eat(',')) throw error("Expected ,");
            double second=enclosed(); return first%second;
        }
        if (word("exp")) return Math.exp(argument());
        if (word("cbrt")) return Math.cbrt(argument());
        if (eat('r')) return Math.sqrt(argument());
        if (eat('p')) return Math.PI;
        if (eat('a')) return answer;
        if (eat('x')) {
            if (Double.isNaN(variable)) throw error("x is only available in calculus tools");
            return variable;
        }
        if (eat('e')) return Math.E;
        throw error("Expected a number or function");
    }

    private double argument() { return eat('(') ? enclosed() : signed(); }
    private double enclosed() { double x = sum(); if (!eat(')')) throw error("Missing )"); return x; }
    private double toAngle(double x) { return degrees ? Math.toRadians(x) : x; }
    private double fromAngle(double x) { return degrees ? Math.toDegrees(x) : x; }
    private double cleanTrig(double result, double angle) { return Math.abs(angle) > 1e-6 && Math.abs(result) < 1e-12 ? 0 : result; }
    private boolean word(String text) { spaces(); if (input.startsWith(text, position)) { position += text.length(); return true; } return false; }
    private boolean nextAtom() {
        spaces();
        if (position == input.length()) return false;
        char c = input.charAt(position);
        return c == '(' || c == 'p' || c == 'e' || c == 'a' || c == 'r' || c == 'x'
                || c == 's' || c == 'c' || c == 't' || c == 'l' || c == 'f' || c == 'm';
    }
    private boolean eat(char c) { spaces(); if (position < input.length() && input.charAt(position) == c) { position++; return true; } return false; }
    private void spaces() { while (position < input.length() && Character.isWhitespace(input.charAt(position))) position++; }
    private IllegalArgumentException error(String message) { return new IllegalArgumentException(message + " at position " + (position + 1)); }

    private static int whole(double n) {
        if (!Double.isFinite(n) || n < 0 || n > 170 || n != Math.rint(n)) throw new IllegalArgumentException("Enter a whole number from 0 to 170");
        return (int)n;
    }
    private static double factorial(double n) { int v = whole(n); double result = 1; for (int i = 2; i <= v; i++) result *= i; return result; }
    private static double permutations(double n, double r) { int a = whole(n), b = whole(r); if (b > a) throw new IllegalArgumentException("r cannot exceed n"); double v = 1; for (int i = 0; i < b; i++) v *= a - i; return v; }
    private static double combinations(double n, double r) { int a = whole(n), b = whole(r); if (b > a) throw new IllegalArgumentException("r cannot exceed n"); b = Math.min(b, a - b); double v = 1; for (int i = 1; i <= b; i++) v = v * (a - b + i) / i; return Math.rint(v); }

    public static String format(double value) {
        if (value == 0) return "0";
        DecimalFormat formatter = new DecimalFormat("0.##########E0", DecimalFormatSymbols.getInstance(Locale.US));
        if (value == Math.rint(value) && Math.abs(value) < 1e12) return String.format(Locale.US, "%.0f", value);
        if (Math.abs(value) >= 1e12 || (Math.abs(value) < 1e-7 && value != 0)) return formatter.format(value);
        return new DecimalFormat("0.##########", DecimalFormatSymbols.getInstance(Locale.US)).format(value);
    }
}
