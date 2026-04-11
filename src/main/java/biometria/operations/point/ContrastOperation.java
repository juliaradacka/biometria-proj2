package biometria.operations.point;

import biometria.model.ImageMatrix;
import biometria.operations.ImageOperation;
import biometria.util.ColorUtil;

public class ContrastOperation implements ImageOperation {

    private final double factor;

    public ContrastOperation(int contrast) {
        this.factor = (259.0 * (contrast + 255.0)) / (255.0 * (259.0 - contrast));;
    }

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

                int newR = ColorUtil.clamp((int) (factor * (red - 128) + 128));
                int newG = ColorUtil.clamp((int) (factor * (green - 128) + 128));
                int newB = ColorUtil.clamp((int) (factor * (blue - 128) + 128));


                output.setARGB(x,y,ColorUtil.toARGB(alpha,newR,newG,newB));
            }
        }
        return output;
    }
}
