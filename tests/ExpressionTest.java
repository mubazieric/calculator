import com.iwat.scientific.Expression;

public class ExpressionTest {
    static void check(String expression, boolean degrees, double expected) {
        double actual = new Expression(expression, degrees, 4).evaluate();
        if (Math.abs(actual - expected) > 1e-9 * Math.max(1, Math.abs(expected)))
            throw new AssertionError(expression + ": expected " + expected + " got " + actual);
    }
    static void fails(String expression) {
        try { new Expression(expression, true, 0).evaluate(); }
        catch (IllegalArgumentException expected) { return; }
        throw new AssertionError("Expected error: " + expression);
    }
    public static void main(String[] args) {
        check("2+3*4", true, 14);
        check("(2+3)^2", true, 25);
        check("2^3^2", true, 512);
        check("-2^2", true, -4);
        check("sin(30)+√(16)", true, 4.5);
        check("sin(π/2)", false, 1);
        check("asin(1)", true, 90);
        check("log(1000)+ln(e)", true, 4);
        check("5 nCr 2", true, 10);
        check("5 nPr 2", true, 20);
        check("5!", true, 120);
        check("2π", true, 2*Math.PI);
        check("2(3+4)", true, 14);
        check("(1+2)(3+4)", true, 21);
        check("Ans^2", true, 16);
        fails("√(-1)"); fails("4/0"); fails("5 nCr 6"); fails("2+");
        System.out.println("Expression tests passed");
    }
}
