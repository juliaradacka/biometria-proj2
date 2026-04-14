package biometria.iris.operations;

import biometria.model.ImageMatrix;
import biometria.operations.ImageOperation;
import biometria.operations.morphology.DilatationOperation;
import biometria.operations.morphology.OpeningOperation;
import biometria.operations.morphology.StructuringElementShape;
import biometria.operations.morphology.RepeatOperation;
import biometria.util.ColorUtil;

public class IrisMaskCleanupOperation implements ImageOperation {

    private static final int BLACK = 0;
    private static final int WHITE = 255;


    private static final int OPENING_SIZE = 5;
    private static final StructuringElementShape SHAPE = StructuringElementShape.ELLIPSE;


    private static final int RECONSTRUCTION_SE_SIZE = 3;
    private static final int RECONSTRUCTION_MAX_ITERATIONS = 500;

    @Override
    public ImageMatrix apply(ImageMatrix input) {
        if (input == null) throw new IllegalArgumentException("Input image cannot be null");


        ImageMatrix disconnected = new RepeatOperation(
                new OpeningOperation(OPENING_SIZE, SHAPE), 1
        ).apply(input.copy());


        int[] center = centerOfBlackMass(disconnected);


        ImageMatrix marker = diskMarker(disconnected.getWidth(), disconnected.getHeight(), center[0], center[1], 10);


        return reconstructByDilatation(marker, disconnected);
    }



    private int[] centerOfBlackMass(ImageMatrix mask) {
        long sx = 0, sy = 0, count = 0;
        int w = mask.getWidth(), h = mask.getHeight();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (ColorUtil.getRed(mask.getARGB(x, y)) == BLACK) {
                    sx += x; sy += y; count++;
                }
            }
        }
        if (count == 0) return new int[]{w / 2, h / 2};
        return new int[]{(int) (sx / count), (int) (sy / count)};
    }

    private ImageMatrix diskMarker(int w, int h, int cx, int cy, int r) {
        int blackArgb = ColorUtil.toARGB(255, BLACK, BLACK, BLACK);
        int whiteArgb = ColorUtil.toARGB(255, WHITE, WHITE, WHITE);
        ImageMatrix out = new ImageMatrix(w, h);

        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) out.setARGB(x, y, whiteArgb);

        int r2 = r * r;
        for (int y = Math.max(0, cy - r); y <= Math.min(h - 1, cy + r); y++) {
            for (int x = Math.max(0, cx - r); x <= Math.min(w - 1, cx + r); x++) {
                if ((x - cx) * (x - cx) + (y - cy) * (y - cy) <= r2) {
                    out.setARGB(x, y, blackArgb);
                }
            }
        }
        return out;
    }

    private ImageMatrix reconstructByDilatation(ImageMatrix marker, ImageMatrix mask) {
        ImageMatrix prev = marker.copy();
        for (int i = 0; i < RECONSTRUCTION_MAX_ITERATIONS; i++) {
            ImageMatrix dil = new DilatationOperation(RECONSTRUCTION_SE_SIZE, SHAPE).apply(prev);
            ImageMatrix next = andMasksBlackObject(dil, mask);

            if (equalsBinary(prev, next)) return next;
            prev = next;
        }
        return prev;
    }

    private boolean equalsBinary(ImageMatrix a, ImageMatrix b) {
        int w = a.getWidth(), h = a.getHeight();
        if (b.getWidth() != w || b.getHeight() != h) return false;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (ColorUtil.getRed(a.getARGB(x, y)) != ColorUtil.getRed(b.getARGB(x, y))) return false;
            }
        }
        return true;
    }

    private ImageMatrix andMasksBlackObject(ImageMatrix a, ImageMatrix b) {
        int w = a.getWidth(), h = a.getHeight();
        ImageMatrix out = new ImageMatrix(w, h);
        int blackArgb = ColorUtil.toARGB(255, BLACK, BLACK, BLACK);
        int whiteArgb = ColorUtil.toARGB(255, WHITE, WHITE, WHITE);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                boolean black = (ColorUtil.getRed(a.getARGB(x, y)) == BLACK) && (ColorUtil.getRed(b.getARGB(x, y)) == BLACK);
                out.setARGB(x, y, black ? blackArgb : whiteArgb);
            }
        }
        return out;
    }
}