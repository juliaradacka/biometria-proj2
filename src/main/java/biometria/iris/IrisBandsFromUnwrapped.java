package biometria.iris;

import biometria.model.ImageMatrix;
import biometria.util.ColorUtil;

public final class IrisBandsFromUnwrapped {

    private IrisBandsFromUnwrapped() {}

    public static final int DEFAULT_BANDS = 8;
    public static final int DEFAULT_POINTS = 128;

    public static double[][] extract(ImageMatrix unwrapped, int bands, int points) {
        if (unwrapped == null) throw new IllegalArgumentException("unwrapped == null");
        if (bands != 8) throw new IllegalArgumentException("this implementation expects bands=8");
        if (points != 128) throw new IllegalArgumentException("this implementation expects points=128");
        if (unwrapped.getWidth() != 360) {
            throw new IllegalArgumentException("unwrap width must be 360 (degrees). got=" + unwrapped.getWidth());
        }

        int w = unwrapped.getWidth();   // 360
        int h = unwrapped.getHeight();  // 64

        int bandHeight = h / 8; // zakładamy 64 -> 8px na pas

        int[] cols1to4 = concat(range(0, 255), range(285, 360));
        int[] cols5to6 = concat(range(0, 57), range(124, 237), range(304, 360));
        int[] cols7to8 = concat(range(0, 45), range(135, 225), range(315, 360));

        double[][] out = new double[8][128];

        for (int bandIdx = 0; bandIdx < 8; bandIdx++) {
            int y0 = bandIdx * bandHeight;
            int y1 = (bandIdx == 7) ? (h - 1) : (y0 + bandHeight - 1);

            int[] validCols;
            if (bandIdx < 4) validCols = cols1to4;
            else if (bandIdx < 6) validCols = cols5to6;
            else validCols = cols7to8;

            // 1) radialne uśrednienie oknem gaussa
            double[] averaged = averageBandRadially(unwrapped, y0, y1, validCols);

            // 2) resampling do 128 punktów
            out[bandIdx] = resampleLinear(averaged, 128);
        }

        return out;
    }

    private static double[] averageBandRadially(ImageMatrix rect, int y0, int y1, int[] cols) {
        int height = y1 - y0 + 1;
        double[] g = gaussianWindow1D(height);

        double[] out = new double[cols.length];

        for (int i = 0; i < cols.length; i++) {
            int x = cols[i];

            double sum = 0.0;
            for (int k = 0; k < height; k++) {
                int y = y0 + k;
                int gray = luminance(rect.getARGB(x, y));
                sum += gray * g[k];
            }
            out[i] = sum;
        }

        return out;
    }

    private static double[] gaussianWindow1D(int size) {
        double sigma = Math.max(size / 3.0, 1.0);
        double[] w = new double[size];
        int c = size / 2;

        double sum = 0.0;
        for (int i = 0; i < size; i++) {
            double x = i - c;
            double v = Math.exp(-(x * x) / (2.0 * sigma * sigma));
            w[i] = v;
            sum += v;
        }
        for (int i = 0; i < size; i++) w[i] /= sum;
        return w;
    }

    private static double[] resampleLinear(double[] src, int newLen) {
        if (src.length == newLen) return src.clone();
        if (src.length == 0) return new double[newLen];

        double[] dst = new double[newLen];

        double scale = (src.length - 1.0) / (newLen - 1.0);

        for (int i = 0; i < newLen; i++) {
            double x = i * scale;
            int x0 = (int) Math.floor(x);
            int x1 = Math.min(x0 + 1, src.length - 1);
            double t = x - x0;
            dst[i] = (1.0 - t) * src[x0] + t * src[x1];
        }

        return dst;
    }

    private static int luminance(int argb) {
        int r = ColorUtil.getRed(argb);
        int g = ColorUtil.getGreen(argb);
        int b = ColorUtil.getBlue(argb);
        return (int) Math.round(0.299 * r + 0.587 * g + 0.114 * b);
    }

    private static int[] range(int aInclusive, int bExclusive) {
        int n = Math.max(0, bExclusive - aInclusive);
        int[] r = new int[n];
        for (int i = 0; i < n; i++) r[i] = aInclusive + i;
        return r;
    }

    private static int[] concat(int[]... arrays) {
        int total = 0;
        for (int[] a : arrays) total += a.length;
        int[] out = new int[total];
        int p = 0;
        for (int[] a : arrays) {
            System.arraycopy(a, 0, out, p, a.length);
            p += a.length;
        }
        return out;
    }
}