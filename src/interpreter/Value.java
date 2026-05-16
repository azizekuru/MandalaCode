// Value.java
package interpreter;

public abstract class Value {

    // Returns a human-readable string for program output and error messages.
    public abstract String display();

    @Override
    public String toString() {
        return display();
    }
}