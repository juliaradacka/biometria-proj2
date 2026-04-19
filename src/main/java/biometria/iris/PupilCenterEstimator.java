package biometria.iris;

import biometria.model.ImageMatrix;
import biometria.util.ColorUtil;

public final class PupilCenterEstimator {

    private static final int BLACK = 0;
    private static final double WIDTH_THRESHOLD_FRACTION = 0.5;

    private PupilCenterEstimator() {}

    public static double[] estimateCenterAndRadius(ImageMatrix pupilMask) {
        if (pupilMask == null) return null;

        int w = pupilMask.getWidth();
        int h = pupilMask.getHeight();
        if (w <= 0 || h <= 0) return null;

        int[] center = centerOfBlackMass(pupilMask);
        int cx = center[0];
        int cy = center[1];

        int[] projX = verticalProjectionBlackCount(pupilMask);
        int[] projY = horizontalProjectionBlackCount(pupilMask);

        int maxX = maxValue(projX);
        int maxY = maxValue(projY);
        if (maxX == 0 || maxY == 0) return null;

        int widthX = widthAtThreshold(projX, cx, (int) Math.round(maxX * WIDTH_THRESHOLD_FRACTION));
        double r = widthX / 2.0;
        return new double[]{cx, cy, r};
    }

    private static int[] centerOfBlackMass(ImageMatrix mask) {
        long sx = 0, sy = 0, count = 0;
        int w = mask.getWidth(), h = mask.getHeight();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (ColorUtil.getRed(mask.getARGB(x, y)) == BLACK) {
                    sx += x;
                    sy += y;
                    count++;
                }
            }
        }

        if (count == 0) return new int[]{w / 2, h / 2};
        return new int[]{(int) (sx / count), (int) (sy / count)};
    }

    public static int[] verticalProjectionBlackCount(ImageMatrix mask) {
        int w = mask.getWidth();
        int h = mask.getHeight();
        int[] proj = new int[w];

        for (int x = 0; x < w; x++) {
            int sum = 0;
            for (int y = 0; y < h; y++) {
                if (ColorUtil.getRed(mask.getARGB(x, y)) == BLACK) sum++;
            }
            proj[x] = sum;
        }
        return proj;
    }

    public static int[] horizontalProjectionBlackCount(ImageMatrix mask) {
        int w = mask.getWidth();
        int h = mask.getHeight();
        int[] proj = new int[h];

        for (int y = 0; y < h; y++) {
            int sum = 0;
            for (int x = 0; x < w; x++) {
                if (ColorUtil.getRed(mask.getARGB(x, y)) == BLACK) sum++;
            }
            proj[y] = sum;
        }
        return proj;
    }

    private static int maxValue(int[] a) {
        int m = 0;
        for (int v : a) if (v > m) m = v;
        return m;
    }

    private static int widthAtThreshold(int[] proj, int centerIndex, int threshold) {
        if (proj == null || proj.length == 0) return 0;
        if (centerIndex < 0 || centerIndex >= proj.length) return 0;
        if (threshold <= 0) threshold = 1;

        int left = centerIndex;
        while (left > 0 && proj[left] >= threshold) left--;
        int right = centerIndex;
        while (right < proj.length - 1 && proj[right] >= threshold) right++;

        int width = (right - 1) - (left + 1) + 1;
        return Math.max(0, width);
    }
}