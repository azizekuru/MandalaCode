// UnaryExpr.java
package ast;

public class UnaryExpr extends Expression {

    public enum Op {
        NEG, // arithmetic negation: -expr
        NOT // boolean negation: !expr
    }

    private final Op op;
    private final Expression operand;

    public UnaryExpr(Op op, Expression operand, int line) {
        super(line);
        this.op = op;
        this.operand = operand;
    }

    public Op getOp() {
        return op;
    }

    public Expression getOperand() {
        return operand;
    }

    @Override
    public String dump(String indent) {
        String childIndent = indent + "  ";
        return indent + "Unary(" + op + ") [line " + getLine() + "]\n"
                + operand.dump(childIndent);
    }
}