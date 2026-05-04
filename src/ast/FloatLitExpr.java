// FloatLitExpr.java
package ast;

public class FloatLitExpr extends Expression {
    private final double value;

    public FloatLitExpr(double value, int line) {
        super(line);
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    @Override
    public String dump(String indent) {
        return indent + "FloatLit(" + value + ") [line " + getLine() + "]";
    }
}