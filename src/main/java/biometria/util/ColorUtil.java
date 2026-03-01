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

    public static int toARGB(int a, int r, int g, int b) {
        a = clamp(a);
        r = clamp(r);
        g = clamp(g);
        b = clamp(b);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int toLuminance(int r, int g, int b) {
        return (int) (0.299 * r + 0.587 * g + 0.114 * b);
    }

    public static int invert(int argb) {
        int a = getAlpha(argb);
        int r = 255 - getRed(argb);
        int g = 255 - getGreen(argb);
        int b = 255 - getBlue(argb);
        return toARGB(a, r, g, b);
    }

    public static int adjustBrightness(int argb, int adjustment) {
        int a = getAlpha(argb);
        int r = clamp(getRed(argb) + adjustment);
        int g = clamp(getGreen(argb) + adjustment);
        int b = clamp(getBlue(argb) + adjustment);
        return toARGB(a, r, g, b);
    }

    public static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private ColorUtil() {
    }
}