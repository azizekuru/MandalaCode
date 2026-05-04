// IntLitExpr.java
package ast;

public class IntLitExpr extends Expression {
    private final int value;

    public IntLitExpr(int value, int line) {
        super(line);
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String dump(String indent) {
        return indent + "IntLit(" + value + ") [line " + getLine() + "]";
    }
}