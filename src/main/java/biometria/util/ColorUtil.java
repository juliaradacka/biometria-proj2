package biometria.util;

public final class ColorUtil {

    public static int getA(int argb) {
        return (argb >> 24) & 0xFF;
    }

    public static int getR(int argb) {
        return (argb >> 16) & 0xFF;
    }

    public static int getG(int argb) {
        return (argb >> 8) & 0xFF;
    }

    public static int getB(int argb) {
        return argb & 0xFF;
    }

    public static int toARGB(int a, int r, int g, int b) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    public static int luminance(int r, int g, int b) {
        return (int) (0.299 * r + 0.587 * g + 0.114 * b);
    }

    public static int average(int r, int g, int b) {
        return (r + g + b) / 3;
    }

    private ColorUtil() {
    }
}