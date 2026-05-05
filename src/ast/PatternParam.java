// PatternParam.java
package ast;

// A single formal parameter: name : type
// Not a Statement or Expression — a lightweight data carrier used by PatternDeclStmt.
public class PatternParam {
    private final String name;
    private final String type;
    private final int line;

    public PatternParam(String name, String type, int line) {
        this.name = name;
        this.type = type;
        this.line = line;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getLine() {
        return line;
    }

    public String dump(String indent) {
        return indent + "Param(" + name + " : " + type + ") [line " + line + "]";
    }
}