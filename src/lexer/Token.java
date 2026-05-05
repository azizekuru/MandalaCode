// Token.java
package lexer;

public class Token {
    private final TokenType type;
    private final String value;
    private final int lineNumber;

    public Token(TokenType type, String value, int lineNumber) {
        this.type = type;
        this.value = value;
        this.lineNumber = lineNumber;
    }

    public TokenType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    // ── Convenience predicates ───────────────────────────────────

    public boolean is(TokenType t) {
        return this.type == t;
    }

    public boolean isAny(TokenType... types) {
        for (TokenType t : types) {
            if (this.type == t)
                return true;
        }
        return false;
    }

    // ── Human-readable form (used by --dump-ast and error messages) ──

    @Override
    public String toString() {
        return "[" + type + " | \"" + value + "\" | line " + lineNumber + "]";
    }
}