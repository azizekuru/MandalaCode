// RadialRepeatStmt.java
package ast;

import java.util.List;

public class RadialRepeatStmt extends Statement {
    private final Expression count;
    private final List<Statement> body;

    public RadialRepeatStmt(Expression count, List<Statement> body, int line) {
        super(line);
        this.count = count;
        this.body = body;
    }

    public Expression getCount() {
        return count;
    }

    public List<Statement> getBody() {
        return body;
    }

    @Override
    public String dump(String indent) {
        String childIndent = indent + "  ";
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("RadialRepeat [line ").append(getLine()).append("]\n");
        sb.append(childIndent).append("count:\n");
        sb.append(count.dump(childIndent + "  ")).append("\n");
        sb.append(childIndent).append("body:");
        for (Statement stmt : body) {
            sb.append("\n").append(stmt.dump(childIndent + "  "));
        }
        return sb.toString();
    }
}