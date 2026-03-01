package biometria.operations.filters;

import biometria.model.ImageMatrix;
import biometria.operations.ImageOperation;
import biometria.util.ColorUtil;

/**
 * Operacja negatywu - inwersja wszystkich kolorów.
 * Każdy kanał RGB jest przekształcany: 255 - wartość
 */
public class NegativeOperation implements ImageOperation {

    @Override
    public ImageMatrix apply(ImageMatrix input) {
        int width = input.getWidth();
        int height = input.getHeight();
        ImageMatrix output = new ImageMatrix(width, height);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = input.getARGB(x, y);
                int inverted = ColorUtil.invert(argb);
                output.setARGB(x, y, inverted);
            }
        }

        return output;
    }
}