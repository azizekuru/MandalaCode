// BoolValue.java
package interpreter;

public class BoolValue extends Value {
    private final boolean value;

    public static final BoolValue TRUE = new BoolValue(true);
    public static final BoolValue FALSE = new BoolValue(false);

    private BoolValue(boolean value) {
        this.value = value;
    }

    // Use the singletons — avoids allocating new BoolValue instances.
    public static BoolValue of(boolean value) {
        return value ? TRUE : FALSE;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public String display() {
        return value ? "true" : "false";
    }
}