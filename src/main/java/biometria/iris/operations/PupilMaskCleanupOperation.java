package biometria.iris.operations;

import biometria.model.ImageMatrix;
import biometria.operations.ImageOperation;
import biometria.operations.morphology.*;
import biometria.util.ColorUtil;

public class PupilMaskCleanupOperation implements ImageOperation {

    private static final int BLACK = 0;
    private static final int WHITE = 255;

    // Parametry
    private static final StructuringElementShape SHAPE = StructuringElementShape.ELLIPSE;

    private static final int PRE_CLOSING_SIZE = 3;
    private static final int PRE_CLOSING_TIMES = 1;

    private static final int PRE_OPENING_SIZE = 3;
    private static final int PRE_OPENING_TIMES = 1;

    private static final int MARKER_RADIUS = 5;

    private static final int RECONSTRUCTION_SE_SIZE = 3;
    private static final int RECONSTRUCTION_MAX_ITERATIONS = 500;

    private static final int HOLE_FILLING_CLOSING_SIZE = 9;
    private static final int HOLE_FILLING_CLOSING_TIMES = 1;

    private static final int FINAL_OPENING_SIZE = 3;
    private static final int FINAL_OPENING_TIMES = 1;

    private static final int SEED_EROSION_SIZE = 3;
    private static final int SEED_EROSION_MAX_STEPS = 80;

    @Override
    public ImageMatrix apply(ImageMatrix input) {
        // 1) preprocessing
        ImageMatrix cleaned = new RepeatOperation(new ClosingOperation(PRE_CLOSING_SIZE, SHAPE), PRE_CLOSING_TIMES)
                .apply(input.copy());

        cleaned = new RepeatOperation(new OpeningOperation(PRE_OPENING_SIZE, SHAPE), PRE_OPENING_TIMES)
                .apply(cleaned);

        // 2) marker: dysk w okolicach środka czarnych pikseli
        int w = cleaned.getWidth();
        int h = cleaned.getHeight();

        ImageMatrix seedCore = lastNonEmptyErosion(cleaned);
        int[] c = centerOfBlackMass(seedCore);

        ImageMatrix marker = diskMarker(w, h, c[0], c[1], MARKER_RADIUS);

        marker = andMasksBlackObject(marker, cleaned);

        if (!hasAnyBlack(marker)) {
            int[] bb = centerOfBlackBoundingBox(cleaned);
            marker = diskMarker(w, h, bb[0], bb[1], Math.max(2, MARKER_RADIUS / 2));
            marker = andMasksBlackObject(marker, cleaned);
        }

        if (!hasAnyBlack(marker)) {
            return cleaned;
        }

        // 3) rekonstrukcja morfologiczna (geodezyjna dylatacja)
        ImageMatrix reconstructed = reconstructByDilatation(marker, cleaned);

        // 4) wypełnianie dziur
        reconstructed = new RepeatOperation(
                new ClosingOperation(HOLE_FILLING_CLOSING_SIZE, SHAPE),
                HOLE_FILLING_CLOSING_TIMES
        ).apply(reconstructed);

        // 5) wygładzenie
        reconstructed = new RepeatOperation(
                new OpeningOperation(FINAL_OPENING_SIZE, SHAPE),
                FINAL_OPENING_TIMES
        ).apply(reconstructed);

        return reconstructed;
    }

    private ImageMatrix lastNonEmptyErosion(ImageMatrix mask) {
        ImageMatrix prev = mask.copy();
        for (int i = 0; i < SEED_EROSION_MAX_STEPS; i++) {
            ImageMatrix next = new ErosionOperation(SEED_EROSION_SIZE, SHAPE).apply(prev);
            if (!hasAnyBlack(next)) return prev; // ostatnia niepusta
            prev = next;
        }
        return prev;
    }

    private ImageMatrix reconstructByDilatation(ImageMatrix marker, ImageMatrix mask) {
        ImageMatrix prev = marker.copy();

        for (int i = 0; i < RECONSTRUCTION_MAX_ITERATIONS; i++) {
            ImageMatrix dil = new DilatationOperation(RECONSTRUCTION_SE_SIZE, SHAPE).apply(prev);
            ImageMatrix next = andMasksBlackObject(dil, mask);

            if (equalsBinary(prev, next)) {
                return next;
            }
            prev = next;
        }
        return prev;
    }

    private boolean equalsBinary(ImageMatrix a, ImageMatrix b) {
        int w = a.getWidth();
        int h = a.getHeight();
        if (b.getWidth() != w || b.getHeight() != h) return false;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int va = ColorUtil.getRed(a.getARGB(x, y));
                int vb = ColorUtil.getRed(b.getARGB(x, y));
                if (va != vb) return false;
            }
        }
        return true;
    }

    private ImageMatrix andMasksBlackObject(ImageMatrix a, ImageMatrix b) {
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
                int va = ColorUtil.getRed(a.getARGB(x, y));
                int vb = ColorUtil.getRed(b.getARGB(x, y));
                boolean black = (va == BLACK) && (vb == BLACK);
                out.setARGB(x, y, black ? blackArgb : whiteArgb);
            }
        }
        return out;
    }

    private boolean hasAnyBlack(ImageMatrix img) {
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                if (ColorUtil.getRed(img.getARGB(x, y)) == BLACK) return true;
            }
        }
        return false;
    }

    private int[] centerOfBlackMass(ImageMatrix mask) {
        long sx = 0, sy = 0, count = 0;

        int w = mask.getWidth();
        int h = mask.getHeight();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (ColorUtil.getRed(mask.getARGB(x, y)) == BLACK) {
                    sx += x;
                    sy += y;
                    count++;
                }
            }
        }

        if (count == 0) return new int[]{w / 2, h / 2};
        return new int[]{(int) (sx / count), (int) (sy / count)};
    }

    private int[] centerOfBlackBoundingBox(ImageMatrix mask) {
        int w = mask.getWidth(), h = mask.getHeight();
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

        if (maxX < 0) return new int[]{w / 2, h / 2};
        return new int[]{(minX + maxX) / 2, (minY + maxY) / 2};
    }

    private ImageMatrix diskMarker(int w, int h, int cx, int cy, int r) {
        int blackArgb = ColorUtil.toARGB(255, BLACK, BLACK, BLACK);
        int whiteArgb = ColorUtil.toARGB(255, WHITE, WHITE, WHITE);

        ImageMatrix out = new ImageMatrix(w, h);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                out.setARGB(x, y, whiteArgb);
            }
        }

        int r2 = r * r;
        for (int y = Math.max(0, cy - r); y <= Math.min(h - 1, cy + r); y++) {
            for (int x = Math.max(0, cx - r); x <= Math.min(w - 1, cx + r); x++) {
                int dx = x - cx;
                int dy = y - cy;
                if (dx * dx + dy * dy <= r2) {
                    out.setARGB(x, y, blackArgb);
                }
            }
        }
        return out;
    }
}