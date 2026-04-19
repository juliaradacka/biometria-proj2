package biometria.iris;

import biometria.model.ImageMatrix;
import biometria.util.ColorUtil;

public class IrisRadiusEstimator {

    private static final int BLACK = 0;

    public static double estimateIrisRadius(ImageMatrix irisMask, int cx, int cy) {
        if (irisMask == null) return 0;

        int w = irisMask.getWidth();
        int h = irisMask.getHeight();

        int[] projX = new int[w];

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

        int xLeft = cx;
        while (xLeft > 0 && projX[xLeft] > 2) {
            xLeft--;
        }
        double radiusLeft = cx - xLeft;

        int xRight = cx;
        while (xRight < w - 1 && projX[xRight] > 2) {
            xRight++;
        }
        double radiusRight = xRight - cx;

        double finalRadius = 0;

        boolean hitLeftWall = (xLeft == 0);
        boolean hitRightWall = (xRight == w - 1);

        if (hitLeftWall && !hitRightWall) {
            finalRadius = radiusRight;
        } else if (!hitLeftWall && hitRightWall) {
            finalRadius = radiusLeft;
        } else {
            if (Math.abs(radiusLeft - radiusRight) > 30) {
                finalRadius = Math.min(radiusLeft, radiusRight);
            } else {
                finalRadius = (radiusLeft + radiusRight) / 2.0;
            }
        }
        return Math.max(0, finalRadius);
    }
}