package biometria.operations.filters;

import biometria.model.ImageMatrix;
import biometria.operations.ImageOperation;
import biometria.util.ColorUtil;

public class BrightnessOperation implements ImageOperation {

    private final int adjustment;

    public BrightnessOperation(int adjustment) {
        this.adjustment = Math.max(-255, Math.min(255, adjustment));
    }

    @Override
    public ImageMatrix apply(ImageMatrix input) {
        int width = input.getWidth();
        int height = input.getHeight();
        ImageMatrix output = new ImageMatrix(width, height);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = input.getARGB(x, y);
                int adjusted = ColorUtil.adjustBrightness(argb, adjustment);
                output.setARGB(x, y, adjusted);
            }
        }

        return output;
    }

    public int getAdjustment() {
        return adjustment;
    }
}