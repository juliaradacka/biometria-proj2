package biometria.iris;

import biometria.model.ImageMatrix;
import biometria.util.ColorUtil;

public class IrisRadiusEstimator {

    private static final int BLACK = 0;

    /**
     * Wylicza promień tęczówki (R_iris) metodą "poziomego skanowania" (od środka w boki).
     */
    public static double estimateIrisRadius(ImageMatrix irisMask, int cx, int cy) {
        if (irisMask == null) return 0;

        int w = irisMask.getWidth();
        int h = irisMask.getHeight();

        if (cx < 0 || cx >= w || cy < 0 || cy >= h) return 0;

        int xLeft = cx;
        while (xLeft > 0 && ColorUtil.getRed(irisMask.getARGB(xLeft, cy)) == BLACK) {
            xLeft--;
        }
        double radiusLeft = cx - xLeft;

        int xRight = cx;
        while (xRight < w - 1 && ColorUtil.getRed(irisMask.getARGB(xRight, cy)) == BLACK) {
            xRight++;
        }
        double radiusRight = xRight - cx;

        return (radiusLeft + radiusRight) / 2.0;
    }
}