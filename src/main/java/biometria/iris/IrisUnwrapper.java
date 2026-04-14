package biometria.iris;

import biometria.model.ImageMatrix;

public class IrisUnwrapper {

    // Standardowe wymiary rozwiniętej tęczówki w systemach biometrycznych
    private static final int UNWRAPPED_WIDTH = 512;
    private static final int UNWRAPPED_HEIGHT = 64;

    public static ImageMatrix unwrap(ImageMatrix sourceImage, int cx, int cy, double rPupil, double rIris) {
        if (sourceImage == null) return null;

        ImageMatrix unwrapped = new ImageMatrix(UNWRAPPED_WIDTH, UNWRAPPED_HEIGHT);

        for (int y = 0; y < UNWRAPPED_HEIGHT; y++) {
            // Ułamek od 0.0 (źrenica) do 1.0 (zewnętrzna krawędź tęczówki)
            double radiusRatio = (double) y / (UNWRAPPED_HEIGHT - 1);

            // Konkretny promień dla tego wiersza
            double currentRadius = rPupil + radiusRatio * (rIris - rPupil);

            for (int x = 0; x < UNWRAPPED_WIDTH; x++) {
                // Kąt w radianach (od 0 do 2*PI)
                double theta = 2.0 * Math.PI * x / UNWRAPPED_WIDTH;

                // Przeliczenie z bieguna na [X, Y] obrazka źródłowego
                int srcX = (int) Math.round(cx + currentRadius * Math.cos(theta));
                int srcY = (int) Math.round(cy + currentRadius * Math.sin(theta));

                // Zabezpieczenie przed wyjściem za zdjęcie (np. ucięte oko)
                if (srcX >= 0 && srcX < sourceImage.getWidth() && srcY >= 0 && srcY < sourceImage.getHeight()) {
                    int argb = sourceImage.getARGB(srcX, srcY);
                    unwrapped.setARGB(x, y, argb);
                } else {
                    // Jeśli wyszliśmy poza obrazek, dajemy czarne tło
                    unwrapped.setARGB(x, y, 0xFF000000);
                }
            }
        }
        return unwrapped;
    }
}