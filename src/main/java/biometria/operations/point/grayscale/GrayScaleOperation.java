package biometria.operations.point.grayscale;

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
                int alpha = ColorUtil.getAlpha(argb);
                int red = ColorUtil.getRed(argb);
                int green = ColorUtil.getGreen(argb);
                int blue = ColorUtil.getBlue(argb);

                int gray = (int)(0.299*red + 0.587*green + 0.114*blue);
                gray = ColorUtil.toARGB(alpha, gray, gray, gray);
                output.setARGB(x,y,gray);
            }
        }
        return output;
    }
}
