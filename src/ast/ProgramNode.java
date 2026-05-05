// ProgramNode.java
package ast;

import java.util.List;

public class ProgramNode extends ASTNode {
    private final List<Statement> topLevel;

    public ProgramNode(List<Statement> topLevel) {
        super(0); // the program root has no meaningful source line
        this.topLevel = topLevel;
    }

    public List<Statement> getTopLevel() {
        return topLevel;
    }

    @Override
    public String dump(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("Program");
        if (topLevel.isEmpty()) {
            sb.append("\n").append(indent).append("  (empty)");
        } else {
            for (Statement stmt : topLevel) {
                sb.append("\n").append(stmt.dump(indent + "  "));
            }
        }
        return sb.toString();
    }
}