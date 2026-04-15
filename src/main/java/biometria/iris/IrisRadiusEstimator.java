package biometria.iris;

import biometria.model.ImageMatrix;
import biometria.util.ColorUtil;

public class IrisRadiusEstimator {

    private static final int BLACK = 0;

    /**
     * Wylicza promień tęczówki (R_iris) za pomocą projekcji pionowej ograniczonej do środkowego paska.
     */
    public static double estimateIrisRadius(ImageMatrix irisMask, int cx, int cy) {
        if (irisMask == null) return 0;

        int w = irisMask.getWidth();
        int h = irisMask.getHeight();

        // Robimy projekcję pionową
        // Nie bierzemy całego obrazu, tylko pasek na wysokości 'cy', czyli środka oka
        int[] projX = new int[w];

        // bierzemy pasek o grubosci 15 pikseli
        int startY = Math.min(h - 1, cy + 5);
        int endY = Math.min(h - 1, cy + 20);

        for (int x = 0; x < w; x++) {
            int blackCount = 0;
            for (int y = startY; y <= endY; y++) {
                if (ColorUtil.getRed(irisMask.getARGB(x, y)) == BLACK) {
                    blackCount++;
                }
            }
            projX[x] = blackCount;
        }


        // Szukamy lewej krawędzi
        int xLeft = cx;
        while (xLeft > 0 && projX[xLeft] > 2) {
            xLeft--;
        }
        double radiusLeft = cx - xLeft;

        // Szukamy prawej krawędzi
        int xRight = cx;
        while (xRight < w - 1 && projX[xRight] > 2) {
            xRight++;
        }
        double radiusRight = xRight - cx;

        // zabezpieczenie przed wlatywaniem w cień
        double finalRadius = 0;

        // Sprawdzamy, czy któryś promień uderzył w ścianę zdjęcia
        boolean hitLeftWall = (xLeft == 0);
        boolean hitRightWall = (xRight == w - 1);

        if (hitLeftWall && !hitRightWall) {
            finalRadius = radiusRight; // ufamy tylko prawej stronie
        } else if (!hitLeftWall && hitRightWall) {
            finalRadius = radiusLeft;  // ufamy tylko lewej
        } else {
            // Żaden nie uderzył w ścianę, ale sprawdzamy asymetrię
            // Cienie zawsze dodają czerni, więc błąd zawsze objawia się za dużym promieniem.
            if (Math.abs(radiusLeft - radiusRight) > 30) {
                // Jeśli różnica to ponad 30 pikseli, jeden promień na pewno wjechał w cień
                // Wybieramy ten mniejszy, bo on zatrzymał się na prawdziwej białkówce
                finalRadius = Math.min(radiusLeft, radiusRight);
            } else {
                // Wszystko w normie, bierzemy średnią
                finalRadius = (radiusLeft + radiusRight) / 2.0;
            }
        }

        return Math.max(0, finalRadius);
    }
}