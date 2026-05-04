// ColorLitExpr.java
package ast;

public class ColorLitExpr extends Expression {
    private final String hexValue;

    public ColorLitExpr(String hexValue, int line) {
        super(line);
        this.hexValue = hexValue;
    }

    public String getHexValue() {
        return hexValue;
    }

    @Override
    public String dump(String indent) {
        return indent + "ColorLit(" + hexValue + ") [line " + getLine() + "]";
    }
}