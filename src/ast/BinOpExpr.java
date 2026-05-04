// BinOpExpr.java
package ast;

public class BinOpExpr extends Expression {

    public enum Op {
        ADD, SUB, MUL, DIV
    }

    private final Op op;
    private final Expression left;
    private final Expression right;

    public BinOpExpr(Op op, Expression left, Expression right, int line) {
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
        return indent + "BinOp(" + op + ") [line " + getLine() + "]\n"
                + left.dump(childIndent) + "\n"
                + right.dump(childIndent);
    }
}