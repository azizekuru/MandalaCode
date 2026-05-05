// AssignStmt.java
package ast;

public class AssignStmt extends Statement {
    private final String name;
    private final Expression value;

    public AssignStmt(String name, Expression value, int line) {
        super(line);
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Expression getValue() {
        return value;
    }

    @Override
    public String dump(String indent) {
        String childIndent = indent + "  ";
        return indent + "Assign(" + name + ") [line " + getLine() + "]\n"
                + childIndent + "value:\n"
                + value.dump(childIndent + "  ");
    }
}