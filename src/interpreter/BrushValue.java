// BrushValue.java
package interpreter;

// Runtime representation of a named or inline brush.
// Holds the three evaluated field values.
public class BrushValue extends Value {
    private final double radius;
    private final double angle;
    private final ColorValue color;

    public BrushValue(double radius, double angle, ColorValue color) {
        this.radius = radius;
        this.angle = angle;
        this.color = color;
    }

    public double getRadius() {
        return radius;
    }

    public double getAngle() {
        return angle;
    }

    public ColorValue getColor() {
        return color;
    }

    @Override
    public String display() {
        return "brush { radius: " + radius
                + ", angle: " + angle
                + ", color: " + color.display() + " }";
    }
}