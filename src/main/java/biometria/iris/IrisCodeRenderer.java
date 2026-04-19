package biometria.iris;

import biometria.model.ImageMatrix;
import biometria.util.ColorUtil;

public final class IrisCodeRenderer {

    private IrisCodeRenderer() {}

    public static ImageMatrix render(boolean[] code, int bands, int points, int scale) {
        int wBits = points * 2;
        int hBits = bands;

        int w = wBits * scale;
        int h = hBits * scale;

        ImageMatrix img = new ImageMatrix(w, h);

        int white = ColorUtil.toARGB(255, 255, 255, 255);
        int black = ColorUtil.toARGB(255, 0, 0, 0);

        int idx = 0;
        for (int b = 0; b < bands; b++) {
            for (int p = 0; p < points; p++) {
                boolean bitRe = code[idx++];
                boolean bitIm = code[idx++];

                fillBlock(img, (2 * p) * scale, b * scale, scale, bitRe ? white : black);
                fillBlock(img, (2 * p + 1) * scale, b * scale, scale, bitIm ? white : black);
            }
        }

        return img;
    }

    private static void fillBlock(ImageMatrix img, int x0, int y0, int s, int argb) {
        for (int yy = y0; yy < y0 + s; yy++) {
            for (int xx = x0; xx < x0 + s; xx++) {
                img.setARGB(xx, yy, argb);
            }
        }
    }
}