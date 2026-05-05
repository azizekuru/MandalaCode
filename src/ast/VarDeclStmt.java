// VarDeclStmt.java
package ast;

public class VarDeclStmt extends Statement {
    private final String name;
    private final String type;
    private final Expression initializer;

    public VarDeclStmt(String name, String type, Expression initializer, int line) {
        super(line);
        this.name = name;
        this.type = type;
        this.initializer = initializer;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Expression getInitializer() {
        return initializer;
    }

    @Override
    public String dump(String indent) {
        String childIndent = indent + "  ";
        return indent + "VarDecl(" + name + " : " + type + ") [line " + getLine() + "]\n"
                + childIndent + "init:\n"
                + initializer.dump(childIndent + "  ");
    }
}