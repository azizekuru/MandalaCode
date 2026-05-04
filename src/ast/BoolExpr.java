// BoolExpr.java
package ast;

public class BoolExpr extends Expression {

    public enum Op {
        // relational
        EQ, NEQ, LT, LTE, GT, GTE,
        // logical
        AND, OR
    }

    private final Op op;
    private final Expression left;
    private final Expression right;

    public BoolExpr(Op op, Expression left, Expression right, int line) {
        super(line);
        this.op = op;
        this.left = left;
        this.right = right;
    }

    public Op getOp() {
        return op;
    }

    public Expression getLeft() {
        return left;
    }

    public Expression getRight() {
        return right;
    }

    @Override
    public String dump(String indent) {
        String childIndent = indent + "  ";
        return indent + "BoolExpr(" + op + ") [line " + getLine() + "]\n"
                + left.dump(childIndent) + "\n"
                + right.dump(childIndent);
    }
}