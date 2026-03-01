package biometria.operations.point;

import biometria.model.ImageMatrix;
import biometria.operations.ImageOperation;
import biometria.util.ColorUtil;

public class NegativeOperation implements ImageOperation {

    @Override
    public ImageMatrix apply(ImageMatrix input) {
        int width = input.getWidth();
        int height = input.getHeight();
        ImageMatrix output = new ImageMatrix(width, height);


        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = input.getARGB(x, y);

                int a = ColorUtil.getAlpha(argb);
                int r = 255 - ColorUtil.getRed(argb);
                int g = 255 - ColorUtil.getGreen(argb);
                int b = 255 - ColorUtil.getBlue(argb);

                int inverted = ColorUtil.toARGB(a, r, g, b);
                output.setARGB(x, y, inverted);
            }
        }
        return output;
    }
}