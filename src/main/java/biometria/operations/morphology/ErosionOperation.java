package biometria.operations.morphology;

import biometria.model.ImageMatrix;
import biometria.operations.ImageOperation;
import biometria.util.ColorUtil;

public class ErosionOperation implements ImageOperation {

    private static final int BLACK = 0;
    private static final int WHITE = 255;

    @Override
    public ImageMatrix apply(ImageMatrix input) {
        int w = input.getWidth();
        int h = input.getHeight();

        ImageMatrix result = input.copy();

        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {

                int maxVal = BLACK;

                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        int argb = input.getARGB(x + kx, y + ky);
                        int gray = ColorUtil.getRed(argb);
                        maxVal = Math.max(maxVal, gray);
                    }
                }

                int v = (maxVal == WHITE) ? WHITE : BLACK;
                result.setARGB(x, y, ColorUtil.toARGB(255, v, v, v));
            }
        }

        return result;
    }
}