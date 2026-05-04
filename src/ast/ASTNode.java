// ASTNode.java
package ast;

public abstract class ASTNode {
    private final int line;

    protected ASTNode(int line) {
        this.line = line;
    }

    public int getLine() {
        return line;
    }

    public abstract String dump(String indent);

    @Override
    public String toString() {
        return dump("");
    }
}