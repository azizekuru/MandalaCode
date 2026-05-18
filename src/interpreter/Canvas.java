package interpreter;

public class Canvas {
    public static final int WIDTH = 100;
    public static final int HEIGHT = 50;
    private static final int CENTER_X = WIDTH / 2; // 50
    private static final int CENTER_Y = HEIGHT / 2; // 25
    private final char[][] grid;

    public Canvas() {
        grid = new char[HEIGHT][WIDTH];
        clear();
    }

    // ── Izgara (Grid) Yönetimi ───────────────────────────────────────────
    public void clear() {
        for (int row = 0; row < HEIGHT; row++) {
            for (int col = 0; col < WIDTH; col++) {
                grid[row][col] = '.';
            }
        }
    }

    // ── Kutupsal → Kartezyen → Izgara Eşlemesi ─────────────────────────
    // Kutupsaldan Kartezyene:
    // x = radius * cos(angle)
    // y = radius * sin(angle)
    //
    // Izgara eşlemesi:
    // col = CENTER_X + round(x * 2.0) — en-boy oranı düzeltmesi:
    // karakterlerin boyu eninden ~2 kat
    // daha uzundur, bu yüzden daireleri
    // yuvarlak tutmak için x, 2 ile çarpılır.
    // row = CENTER_Y - round(y) — satırlar aşağı doğru artar,
    // bu yüzden y'nin negatifi alınır.
    public void drawPoint(double radius, double angleDegrees) {
        double angleRadians = Math.toRadians(angleDegrees);
        double cartX = radius * Math.cos(angleRadians);
        double cartY = radius * Math.sin(angleRadians);

        int col = CENTER_X + (int) Math.round(cartX * 2.0);
        int row = CENTER_Y - (int) Math.round(cartY);

        if (row >= 0 && row < HEIGHT && col >= 0 && col < WIDTH) {
            grid[row][col] = '*';
        }
    }

    // ── radial_repeat yardımcı metodu ─────────────────────────────────────
    public void drawRadialPoints(double radius, int count) {
        if (count <= 0)
            return;
        double step = 360.0 / count;
        for (int i = 0; i < count; i++) {
            drawPoint(radius, i * step);
        }
    }

    // ── Başlangıç noktası (Origin) işaretleyicisi ────────────────────────
    public void markOrigin() {
        grid[CENTER_Y][CENTER_X] = '+';
    }

    // ── Yazdır (Print) ───────────────────────────────────────────────────
    public void print() {
        System.out.print('+');
        for (int col = 0; col < WIDTH; col++)
            System.out.print('-');
        System.out.println('+');

        for (int row = 0; row < HEIGHT; row++) {
            System.out.print('|');
            for (int col = 0; col < WIDTH; col++) {
                System.out.print(grid[row][col]);
            }
            System.out.println('|');
        }

        System.out.print('+');
        for (int col = 0; col < WIDTH; col++)
            System.out.print('-');
        System.out.println('+');
    }
}