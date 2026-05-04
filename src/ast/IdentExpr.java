// IdentExpr.java
package ast;

public class IdentExpr extends Expression {
    private final String name;

    public IdentExpr(String name, int line) {
        super(line);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String dump(String indent) {
        return indent + "Ident(" + name + ") [line " + getLine() + "]";
    }
}