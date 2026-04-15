package biometria.iris.operations;

import biometria.model.ImageMatrix;
import biometria.util.ColorUtil;

/**
 * Pomocnicze operacje na binarnych maskach, gdzie:
 *  - obiekt = BLACK (0)
 *  - tło    = WHITE (255)
 */
public final class BinaryMaskOps {

    public static final int BLACK = 0;
    public static final int WHITE = 255;

    private BinaryMaskOps() {}

    /**
     * Zwraca bbox czarnych pikseli: {minX, minY, maxX, maxY}.
     * Jeśli brak czarnych pikseli -> zwraca null.
     */
    public static int[] blackBoundingBox(ImageMatrix mask) {
        int w = mask.getWidth();
        int h = mask.getHeight();

        int minX = w, minY = h, maxX = -1, maxY = -1;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (ColorUtil.getRed(mask.getARGB(x, y)) == BLACK) {
                    if (x < minX) minX = x;
                    if (y < minY) minY = y;
                    if (x > maxX) maxX = x;
                    if (y > maxY) maxY = y;
                }
            }
        }

        if (maxX < 0) return null;
        return new int[]{minX, minY, maxX, maxY};
    }

    public static ImageMatrix orBlack(ImageMatrix a, ImageMatrix b) {
        int w = a.getWidth();
        int h = a.getHeight();
        if (b.getWidth() != w || b.getHeight() != h) {
            throw new IllegalArgumentException("Mask sizes differ");
        }

        ImageMatrix out = new ImageMatrix(w, h);
        int blackArgb = ColorUtil.toARGB(255, BLACK, BLACK, BLACK);
        int whiteArgb = ColorUtil.toARGB(255, WHITE, WHITE, WHITE);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                boolean black = (ColorUtil.getRed(a.getARGB(x, y)) == BLACK)
                        || (ColorUtil.getRed(b.getARGB(x, y)) == BLACK);
                out.setARGB(x, y, black ? blackArgb : whiteArgb);
            }
        }
        return out;
    }

    /**
     * AND dla masek (obiekt czarny): wynik jest czarny tylko tam,
     * gdzie oba wejścia mają czarny piksel.
     */
    public static ImageMatrix andBlack(ImageMatrix a, ImageMatrix b) {
        int w = a.getWidth();
        int h = a.getHeight();
        if (b.getWidth() != w || b.getHeight() != h) {
            throw new IllegalArgumentException("Mask sizes differ");
        }

        ImageMatrix out = new ImageMatrix(w, h);
        int blackArgb = ColorUtil.toARGB(255, BLACK, BLACK, BLACK);
        int whiteArgb = ColorUtil.toARGB(255, WHITE, WHITE, WHITE);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                boolean black = ColorUtil.getRed(a.getARGB(x, y)) == BLACK
                        && ColorUtil.getRed(b.getARGB(x, y)) == BLACK;
                out.setARGB(x, y, black ? blackArgb : whiteArgb);
            }
        }
        return out;
    }

    /**
     * Zwraca kopię maski, gdzie czarne piksele z 'toRemove' są usuwane (ustawiane na białe)
     * w masce 'base'. Czyli: base \ toRemove.
     */
    public static ImageMatrix subtractBlack(ImageMatrix base, ImageMatrix toRemove) {
        int w = base.getWidth();
        int h = base.getHeight();
        if (toRemove.getWidth() != w || toRemove.getHeight() != h) {
            throw new IllegalArgumentException("Mask sizes differ");
        }

        ImageMatrix out = base.copy();
        int whiteArgb = ColorUtil.toARGB(255, WHITE, WHITE, WHITE);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (ColorUtil.getRed(toRemove.getARGB(x, y)) == BLACK) {
                    out.setARGB(x, y, whiteArgb);
                }
            }
        }
        return out;
    }

    /**
     * Ustawia wszystko poza ROI na białe (tło). ROI jest włącznie.
     */
    public static ImageMatrix keepOnlyRoi(ImageMatrix mask, int x0, int y0, int x1, int y1) {
        int w = mask.getWidth();
        int h = mask.getHeight();

        int rx0 = clamp(x0, 0, w - 1);
        int ry0 = clamp(y0, 0, h - 1);
        int rx1 = clamp(x1, 0, w - 1);
        int ry1 = clamp(y1, 0, h - 1);

        ImageMatrix out = mask.copy();
        int whiteArgb = ColorUtil.toARGB(255, WHITE, WHITE, WHITE);

        for (int y = 0; y < h; y++) {
            boolean yIn = (y >= ry0 && y <= ry1);
            for (int x = 0; x < w; x++) {
                boolean in = yIn && (x >= rx0 && x <= rx1);
                if (!in) out.setARGB(x, y, whiteArgb);
            }
        }
        return out;
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}