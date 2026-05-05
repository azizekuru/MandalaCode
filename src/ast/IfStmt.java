// IfStmt.java
package ast;

import java.util.List;

public class IfStmt extends Statement {
    private final BoolExpr condition;
    private final List<Statement> thenBlock;
    private final List<Statement> elseBlock; // null if no else branch

    public IfStmt(BoolExpr condition,
            List<Statement> thenBlock,
            List<Statement> elseBlock,
            int line) {
        super(line);
        this.condition = condition;
        this.thenBlock = thenBlock;
        this.elseBlock = elseBlock;
    }

    public BoolExpr getCondition() {
        return condition;
    }

    public List<Statement> getThenBlock() {
        return thenBlock;
    }

    public List<Statement> getElseBlock() {
        return elseBlock;
    } // may be null

    public boolean hasElse() {
        return elseBlock != null;
    }

    @Override
    public String dump(String indent) {
        String childIndent = indent + "  ";
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("If [line ").append(getLine()).append("]\n");
        sb.append(childIndent).append("condition:\n");
        sb.append(condition.dump(childIndent + "  ")).append("\n");
        sb.append(childIndent).append("then:");
        for (Statement stmt : thenBlock) {
            sb.append("\n").append(stmt.dump(childIndent + "  "));
        }
        if (hasElse()) {
            sb.append("\n").append(childIndent).append("else:");
            for (Statement stmt : elseBlock) {
                sb.append("\n").append(stmt.dump(childIndent + "  "));
            }
        }
        return sb.toString();
    }
}