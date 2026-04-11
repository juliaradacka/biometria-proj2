package biometria.operations.point;

import biometria.model.ImageMatrix;
import biometria.operations.ImageOperation;
import biometria.util.ColorUtil;

public class BinarizationOperation implements ImageOperation {

    private final int threshold;

    public BinarizationOperation(int threshold) {
        this.threshold = threshold;
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
                    int r = ColorUtil.getRed(argb);
                    int g = ColorUtil.getGreen(argb);
                    int b = ColorUtil.getBlue(argb);

                    int luminence = (int) (0.299 * r + 0.587 * g + 0.114 * b);

                    int newVal = (luminence >= threshold) ? 255 : 0;

                    output.setARGB(x,y,ColorUtil.toARGB(newVal,newVal,newVal));

                }
            }
            return output;

        }
}
