package biometria.iris;

import biometria.model.ImageMatrix;
import biometria.util.ColorUtil;

public class IrisAnalyzer {

    /**
     * Oblicza średnią jasność obrazu (parametr P we wzorze Daugmana).
     */
    public static double calculateAverageBrightness(ImageMatrix img) {
        long sum = 0;
        int width = img.getWidth();
        int height = img.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = ColorUtil.getRed(img.getARGB(x, y));
                sum += r;
            }
        }
        return (double) sum / (width * height);
    }
}
