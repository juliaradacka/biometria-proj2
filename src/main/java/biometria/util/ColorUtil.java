package biometria.util;

public final class ColorUtil {

    public static int getAlpha(int argb) {
        return (argb >> 24) & 0xFF;
    }

    public static int getRed(int argb) {
        return (argb >> 16) & 0xFF;
    }

    public static int getGreen(int argb) {
        return (argb >> 8) & 0xFF;
    }

    public static int getBlue(int argb) {
        return argb & 0xFF;
    }

    public static int toARGB(int r, int g, int b) {
        return toARGB(255, r, g, b);
    }

    public static int toARGB(int a, int r, int g, int b) {
        return (clamp(a) << 24) |
                (clamp(r) << 16) |
                (clamp(g) << 8)  |
                clamp(b);
    }

    public static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private ColorUtil() {
        throw new AssertionError("Utility class");
    }
}