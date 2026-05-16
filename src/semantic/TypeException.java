// TypeException.java
package semantic;

public class TypeException extends RuntimeException {

    private final int line;

    public TypeException(String message, int line) {
        super("[Type error] Line " + line + ": " + message);
        this.line = line;
    }

    public int getLine() {
        return line;
    }
}