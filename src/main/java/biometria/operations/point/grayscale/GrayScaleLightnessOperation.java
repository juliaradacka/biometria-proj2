package biometria.operations.point.grayscale;

import biometria.model.ImageMatrix;
import biometria.operations.ImageOperation;
import biometria.util.ColorUtil;

public class GrayScaleLightnessOperation implements ImageOperation {
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

                int max = Math.max(r, Math.max(g, b));
                int min = Math.min(r, Math.min(g, b));

                int lightness = (max + min) / 2;
                output.setARGB(x, y, ColorUtil.toARGB(lightness, lightness, lightness));
            }
        }
        return output;
    }
}