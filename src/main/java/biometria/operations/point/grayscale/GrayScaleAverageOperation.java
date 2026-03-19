package biometria.operations.point.grayscale;

import biometria.model.ImageMatrix;
import biometria.operations.ImageOperation;
import biometria.util.ColorUtil;

public class GrayScaleAverageOperation implements ImageOperation {
    @Override
    public ImageMatrix apply(ImageMatrix input) {
        int w = input.getWidth();
        int h = input.getHeight();
        ImageMatrix output = new ImageMatrix(w, h);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = input.getARGB(x, y);
                int r = ColorUtil.getRed(argb);
                int g = ColorUtil.getGreen(argb);
                int b = ColorUtil.getBlue(argb);

                int avg = (r + g + b) / 3;
                output.setARGB(x, y, ColorUtil.toARGB(avg, avg, avg));
            }
        }
        return output;
    }
}