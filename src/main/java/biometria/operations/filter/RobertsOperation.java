package biometria.operations.filter;

import biometria.model.ImageMatrix;
import biometria.operations.ImageOperation;
import biometria.operations.point.GrayScaleOperation;
import biometria.util.ColorUtil;

public class RobertsOperation implements ImageOperation {
    @Override
    public ImageMatrix apply(ImageMatrix input) {
        ImageMatrix gray = new GrayScaleOperation().apply(input);
        int w = gray.getWidth();
        int h = gray.getHeight();
        ImageMatrix output = new ImageMatrix(w, h);

        for (int y = 0; y < h - 1; y++) {
            for (int x = 0; x < w - 1; x++) {
                int p00 = ColorUtil.getRed(gray.getARGB(x, y));
                int p10 = ColorUtil.getRed(gray.getARGB(x + 1, y));
                int p01 = ColorUtil.getRed(gray.getARGB(x, y + 1));
                int p11 = ColorUtil.getRed(gray.getARGB(x + 1, y + 1));

                int gx = p00 - p11;
                int gy = p10 - p01;

                int magnitude = (int) Math.sqrt(gx * gx + gy * gy);
                magnitude = Math.min(255, Math.abs(magnitude));
                output.setARGB(x, y, ColorUtil.toARGB(magnitude, magnitude, magnitude));
            }
        }
        return output;
    }
}
