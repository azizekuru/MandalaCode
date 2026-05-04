// DrawStmt.java
package ast;

public class DrawStmt extends Statement {

    // true → draw namedBrush;
    // false → draw brush { radius: ..., angle: ..., color: ... };
    private final boolean isNamed;

    // used when isNamed == true
    private final String brushName;

    // used when isNamed == false
    private final Expression radius;
    private final Expression angle;
    private final Expression color;

    // Constructor 1 — named brush reference
    public DrawStmt(String brushName, int line) {
        super(line);
        this.isNamed = true;
        this.brushName = brushName;
        this.radius = null;
        this.angle = null;
        this.color = null;
    }

    // Constructor 2 — inline brush literal
    public DrawStmt(Expression radius,
            Expression angle,
            Expression color,
            int line) {
        super(line);
        this.isNamed = false;
        this.brushName = null;
        this.radius = radius;
        this.angle = angle;
        this.color = color;
    }

    public boolean isNamed() {
        return isNamed;
    }

    public String getBrushName() {
        return brushName;
    }

    public Expression getRadius() {
        return radius;
    }

    public Expression getAngle() {
        return angle;
    }

    public Expression getColor() {
        return color;
    }

    @Override
    public String dump(String indent) {
        String childIndent = indent + "  ";
        if (isNamed) {
            return indent + "Draw(named: " + brushName + ") [line " + getLine() + "]";
        }
        return indent + "Draw(inline) [line " + getLine() + "]\n"
                + childIndent + "radius:\n" + radius.dump(childIndent + "  ") + "\n"
                + childIndent + "angle:\n" + angle.dump(childIndent + "  ") + "\n"
                + childIndent + "color:\n" + color.dump(childIndent + "  ");
    }
}