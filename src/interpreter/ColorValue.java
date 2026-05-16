// ColorValue.java
package interpreter;

public class ColorValue extends Value {

    // Stored as the original hex string for rendering (e.g. "#FF4500").
    private final String hex;

    // Parsed RGB components — used by the canvas renderer in Part 2.
    private final int r;
    private final int g;
    private final int b;

    public ColorValue(String hex) {
        this.hex = hex.startsWith("#") ? hex : "#" + hex;
        // Parse the three byte pairs from the hex string.
        this.r = Integer.parseInt(this.hex.substring(1, 3), 16);
        this.g = Integer.parseInt(this.hex.substring(3, 5), 16);
        this.b = Integer.parseInt(this.hex.substring(5, 7), 16);
    }

    public String getHex() {
        return hex;
    }

    public int getR() {
        return r;
    }

    public int getG() {
        return g;
    }

    public int getB() {
        return b;
    }

    @Override
    public String display() {
        return hex;
    }
}