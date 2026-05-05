// PatternDeclStmt.java
package ast;

import java.util.List;

public class PatternDeclStmt extends Statement {
    private final String name;
    private final List<PatternParam> params;
    private final List<Statement> body;

    public PatternDeclStmt(String name,
            List<PatternParam> params,
            List<Statement> body,
            int line) {
        super(line);
        this.name = name;
        this.params = params;
        this.body = body;
    }

    public String getName() {
        return name;
    }

    public List<PatternParam> getParams() {
        return params;
    }

    public List<Statement> getBody() {
        return body;
    }

    @Override
    public String dump(String indent) {
        String childIndent = indent + "  ";
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("PatternDecl(").append(name).append(") [line ")
                .append(getLine()).append("]");
        if (params.isEmpty()) {
            sb.append("\n").append(childIndent).append("params: (none)");
        } else {
            sb.append("\n").append(childIndent).append("params:");
            for (PatternParam p : params) {
                sb.append("\n").append(p.dump(childIndent + "  "));
            }
        }
        sb.append("\n").append(childIndent).append("body:");
        for (Statement stmt : body) {
            sb.append("\n").append(stmt.dump(childIndent + "  "));
        }
        return sb.toString();
    }
}