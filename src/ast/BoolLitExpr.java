// BoolLitExpr.java — new file
package ast;

public class BoolLitExpr extends Expression {
    private final boolean value;

    public BoolLitExpr(boolean value, int line) {
        super(line);
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public String dump(String indent) {
        return indent + "BoolLit(" + value + ") [line " + getLine() + "]";
    }
}