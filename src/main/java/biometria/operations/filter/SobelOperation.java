package biometria.operations.filter;

import biometria.model.ImageMatrix;
import biometria.operations.ImageOperation;
import biometria.operations.point.GrayScaleOperation;
import biometria.util.ColorUtil;

public class SobelOperation implements ImageOperation {
    @Override
    public ImageMatrix apply(ImageMatrix input) {
        ImageMatrix gray = new GrayScaleOperation().apply(input);
        int w = gray.getWidth();
        int h = gray.getHeight();
        // nowa matryca, na której będą rysowane krawędzie
        ImageMatrix output = new ImageMatrix(w,h);

        int[][] GxMask = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}};
        int[][] GyMask = {{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}};

        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                int gxSum = 0;
                int gySum = 0;

                for (int j = -1; j <= 1; j++) {
                    for (int i = -1; i <= 1; i++) {
                        int luminance = ColorUtil.getRed(gray.getARGB(x + i, y + j));
                        gxSum += luminance * GxMask[j + 1][i + 1];
                        gySum += luminance * GyMask[j + 1][i + 1];
                    }
                }
                int magnitude = (int) Math.sqrt(gxSum * gxSum + gySum * gySum);
                magnitude = Math.min(255, magnitude);
                output.setARGB(x, y, ColorUtil.toARGB(magnitude, magnitude, magnitude));
            }
        }
        return output;



    }
}
