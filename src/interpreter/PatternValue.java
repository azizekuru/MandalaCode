// PatternValue.java
package interpreter;

import ast.PatternDeclStmt;

// Runtime representation of a pattern declaration.
// Stores the original AST node so the interpreter can
// retrieve the parameter list and body at call time.
public class PatternValue extends Value {
    private final PatternDeclStmt decl;

    public PatternValue(PatternDeclStmt decl) {
        this.decl = decl;
    }

    public PatternDeclStmt getDecl() {
        return decl;
    }

    @Override
    public String display() {
        return "pattern(" + decl.getName() + ")";
    }
}