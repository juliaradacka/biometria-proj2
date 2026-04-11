package biometria.operations.point;

import biometria.model.ImageMatrix;
import biometria.operations.ImageOperation;
import biometria.util.ColorUtil;

public class BrightnessOperation implements ImageOperation {

    private final int offset;

    public BrightnessOperation(int offset) {
        this.offset = offset;
    }


    @Override
    public ImageMatrix apply(ImageMatrix input) {
        int width = input.getWidth();
        int height = input.getHeight();
        ImageMatrix output = new ImageMatrix(width, height);

        for (int y=0; y <height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = input.getARGB(x, y);
                int alpha = ColorUtil.getAlpha(argb);
                int red = ColorUtil.getRed(argb);
                int green = ColorUtil.getGreen(argb);
                int blue = ColorUtil.getBlue(argb);

                int newR = ColorUtil.clamp(red + offset);
                int newB = ColorUtil.clamp(blue + offset);
                int newG = ColorUtil.clamp(green + offset);

                output.setARGB(x, y, ColorUtil.toARGB(alpha, newR, newG, newB));
            }
        }
        return output;

    }
}
