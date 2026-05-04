// BrushDeclStmt.java
package ast;

public class BrushDeclStmt extends Statement {
    private final String name;
    private final Expression radius;
    private final Expression angle;
    private final Expression color;

    public BrushDeclStmt(String name,
            Expression radius,
            Expression angle,
            Expression color,
            int line) {
        super(line);
        this.name = name;
        this.radius = radius;
        this.angle = angle;
        this.color = color;
    }

    public String getName() {
        return name;
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
        return indent + "BrushDecl(" + name + ") [line " + getLine() + "]\n"
                + childIndent + "radius:\n" + radius.dump(childIndent + "  ") + "\n"
                + childIndent + "angle:\n" + angle.dump(childIndent + "  ") + "\n"
                + childIndent + "color:\n" + color.dump(childIndent + "  ");
    }
}