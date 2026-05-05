// PatternCallStmt.java
package ast;

import java.util.List;

public class PatternCallStmt extends Statement {
    private final String callee;
    private final List<Expression> args;

    public PatternCallStmt(String callee, List<Expression> args, int line) {
        super(line);
        this.callee = callee;
        this.args = args;
    }

    public String getCallee() {
        return callee;
    }

    public List<Expression> getArgs() {
        return args;
    }

    @Override
    public String dump(String indent) {
        String childIndent = indent + "  ";
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("PatternCall(").append(callee).append(") [line ")
                .append(getLine()).append("]");
        if (args.isEmpty()) {
            sb.append("\n").append(childIndent).append("args: (none)");
        } else {
            sb.append("\n").append(childIndent).append("args:");
            for (int i = 0; i < args.size(); i++) {
                sb.append("\n").append(args.get(i).dump(childIndent + "  "));
            }
        }
        return sb.toString();
    }
}