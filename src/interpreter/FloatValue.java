// FloatValue.java
package interpreter;

// Used internally for both 'radius' and 'angle' declared types.
// The type distinction is enforced by the type checker; at runtime
// both are real-valued numbers and share this representation.
public class FloatValue extends Value {
    private final double value;

    public FloatValue(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    @Override
    public String display() {
        // Suppress trailing zeros for clean output: 45.0 → "45.0", 3.14 → "3.14"
        return String.valueOf(value);
    }
}