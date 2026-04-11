package biometria.operations.morphology;

import biometria.model.ImageMatrix;
import biometria.operations.ImageOperation;
import biometria.util.ColorUtil;

public class ErosionOperation implements ImageOperation {

    private static final int BLACK = 0;
    private static final int WHITE = 255;

    private final boolean[][] se;
    private final int size;
    private final int center;

    public ErosionOperation(int size, StructuringElementShape shape) {
        this.size = size;
        this.center = size / 2;
        this.se = StructuringElementFactory.create(size, shape);
    }

    @Override
    public ImageMatrix apply(ImageMatrix input) {
        int w = input.getWidth();
        int h = input.getHeight();

        ImageMatrix result = new ImageMatrix(w, h);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int maxVal = BLACK;

                for (int sy = 0; sy < size; sy++) {
                    for (int sx = 0; sx < size; sx++) {
                        if (!se[sy][sx]) continue;

                        int xx = clamp(x + (sx - center), 0, w - 1); // edge padding
                        int yy = clamp(y + (sy - center), 0, h - 1);

                        int gray = ColorUtil.getRed(input.getARGB(xx, yy));
                        maxVal = Math.max(maxVal, gray);
                        if (maxVal == WHITE) break; // szybkie wyjście
                    }
                    if (maxVal == WHITE) break;
                }

                int v = (maxVal == WHITE) ? WHITE : BLACK;
                result.setARGB(x, y, ColorUtil.toARGB(255, v, v, v));
            }
        }

        return result;
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}