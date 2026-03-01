package biometria.operations.filters;

import biometria.model.ImageMatrix;
import biometria.operations.ImageOperation;
import biometria.util.ColorUtil;

public class GrayScaleOperation implements ImageOperation {

    @Override
    public ImageMatrix apply(ImageMatrix input) {
        int width = input.getWidth();
        int height = input.getHeight();
        ImageMatrix output = new ImageMatrix(width, height);

        for (int y=0; y <height; y++) {
            for (int x=0; x<width; x++) {
                int argb = input.getARGB(x,y);
                int gray = convertToGray(argb);
                output.setARGB(x,y,gray);
            }
        }
        return output;
    }

    private int convertToGray(int argb) {
        int alpha = ColorUtil.getAlpha(argb);
        int red = ColorUtil.getRed(argb);
        int green = ColorUtil.getGreen(argb);
        int blue = ColorUtil.getBlue(argb);
        int gray;

        // lumiance
        gray = ColorUtil.toLuminance(red, green, blue);
        return ColorUtil.toARGB(alpha, gray, gray, gray);
    }
}
