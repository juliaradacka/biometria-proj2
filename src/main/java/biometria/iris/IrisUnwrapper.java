package biometria.iris;

import biometria.model.ImageMatrix;

public class IrisUnwrapper {

    // 360, żeby 1 kolumna = 1 stopień
    private static final int UNWRAPPED_WIDTH = 360;
    private static final int UNWRAPPED_HEIGHT = 64;

    public static ImageMatrix unwrap(ImageMatrix sourceImage, int cx, int cy, double rPupil, double rIris) {
        if (sourceImage == null) return null;

        ImageMatrix unwrapped = new ImageMatrix(UNWRAPPED_WIDTH, UNWRAPPED_HEIGHT);

        for (int y = 0; y < UNWRAPPED_HEIGHT; y++) {

            double radiusRatio = (double) y / (UNWRAPPED_HEIGHT - 1);
            double currentRadius = rPupil + radiusRatio * (rIris - rPupil);

            for (int x = 0; x < UNWRAPPED_WIDTH; x++) {
                double theta = 2.0 * Math.PI * x / UNWRAPPED_WIDTH;

                int srcX = (int) Math.round(cx + currentRadius * Math.cos(theta));
                int srcY = (int) Math.round(cy + currentRadius * Math.sin(theta));

                if (srcX >= 0 && srcX < sourceImage.getWidth() && srcY >= 0 && srcY < sourceImage.getHeight()) {
                    int argb = sourceImage.getARGB(srcX, srcY);
                    unwrapped.setARGB(x, y, argb);
                } else {
                    unwrapped.setARGB(x, y, 0xFF000000);
                }
            }
        }
        return unwrapped;
    }
}