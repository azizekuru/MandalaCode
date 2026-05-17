// RuntimeError.java
package interpreter;

public class RuntimeError extends RuntimeException {
    private final int line;

    public RuntimeError(String message, int line) {
        super("[Runtime error] Line " + line + ": " + message);
        this.line = line;
    }

    public int getLine() {
        return line;
    }
}