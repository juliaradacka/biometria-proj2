package biometria.gui;

import biometria.model.ImageMatrix;
import biometria.util.ColorUtil;

public class Projections {

    public static int[] horizontal(ImageMatrix image, int threshold, boolean objectIsDark) {
        int w = image.getWidth();
        int h = image.getHeight();
        int[] proj = new int[h];

        for (int y = 0; y < h; y++) {
            int count = 0;
            for (int x = 0; x < w; x++) {
                int lum = luminance(image.getARGB(x, y));
                if (isObjectPixel(lum, threshold, objectIsDark)) {
                    count++;
                }
            }
            proj[y] = count;
        }
        return proj;
    }

    public static int[] vertical(ImageMatrix image, int threshold, boolean objectIsDark) {
        int w = image.getWidth();
        int h = image.getHeight();
        int[] proj = new int[w];

        for (int x = 0; x < w; x++) {
            int count = 0;
            for (int y = 0; y < h; y++) {
                int lum = luminance(image.getARGB(x, y));
                if (isObjectPixel(lum, threshold, objectIsDark)) {
                    count++;
                }
            }
            proj[x] = count;
        }
        return proj;
    }

    private static int luminance(int argb) {
        int r = ColorUtil.getRed(argb);
        int g = ColorUtil.getGreen(argb);
        int b = ColorUtil.getBlue(argb);
        return (int) (0.299 * r + 0.587 * g + 0.114 * b);
    }

    private static boolean isObjectPixel(int luminance, int threshold, boolean objectIsDark) {
        return objectIsDark ? (luminance < threshold) : (luminance >= threshold);
    }

    private Projections() {
        throw new AssertionError("Utility class");
    }
}
