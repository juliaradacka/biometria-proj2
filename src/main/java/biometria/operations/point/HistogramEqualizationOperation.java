package biometria.operations.point;

import biometria.model.ImageMatrix;
import biometria.operations.ImageOperation;
import biometria.util.ColorUtil;

public class HistogramEqualizationOperation implements ImageOperation {
    @Override
    public ImageMatrix apply(ImageMatrix input) {
        int w = input.getWidth();
        int h = input.getHeight();
        int totalPixels = w * h;
        ImageMatrix output = new ImageMatrix(w, h);

        // zliczamy wystąpienia każdego koloru (osobno dla R, G, B)
        int[] histR = new int[256];
        int[] histG = new int[256];
        int[] histB = new int[256];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = input.getARGB(x, y);
                histR[ColorUtil.getRed(argb)]++;
                histG[ColorUtil.getGreen(argb)]++;
                histB[ColorUtil.getBlue(argb)]++;
            }
        }

        // skumulowana suma pikseli
        int[] cdfR = new int[256];
        int[] cdfG = new int[256];
        int[] cdfB = new int[256];

        cdfR[0] = histR[0]; cdfG[0] = histG[0]; cdfB[0] = histB[0];
        for (int i = 1; i < 256; i++) {
            cdfR[i] = cdfR[i - 1] + histR[i];
            cdfG[i] = cdfG[i - 1] + histG[i];
            cdfB[i] = cdfB[i - 1] + histB[i];
        }

        // szukamy pierwszej niezerowej wartości w dystrybuancie
        int cdfMinR = getMinNonZero(cdfR);
        int cdfMinG = getMinNonZero(cdfG);
        int cdfMinB = getMinNonZero(cdfB);

        // transformacja pikseli nowym wzorem
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = input.getARGB(x, y);
                int r = ColorUtil.getRed(argb);
                int g = ColorUtil.getGreen(argb);
                int b = ColorUtil.getBlue(argb);

                int newR = Math.round(((float)(cdfR[r] - cdfMinR) / (totalPixels - cdfMinR)) * 255);
                int newG = Math.round(((float)(cdfG[g] - cdfMinG) / (totalPixels - cdfMinG)) * 255);
                int newB = Math.round(((float)(cdfB[b] - cdfMinB) / (totalPixels - cdfMinB)) * 255);

                // zabezpieczenie przed przekroczeniem zakresu
                newR = Math.min(255, Math.max(0, newR));
                newG = Math.min(255, Math.max(0, newG));
                newB = Math.min(255, Math.max(0, newB));

                output.setARGB(x, y, ColorUtil.toARGB(newR, newG, newB));
            }
        }
        return output;
    }

    private int getMinNonZero(int[] cdf) {
        for (int val : cdf) {
            if (val > 0) return val;
        }
        return 1;
    }
}