package biometria.iris;

public final class IrisCode {

    private IrisCode() {}

    /**
     * buduje kod binarny z sygnałów pasów (8x128) przez gabora
     * - 2 bity na punkt: (re>=0), (im>=0)
     * - pas jest centrowany (zero-mean) i normalizowany (std=1), żeby uniknąć biasu
     */
    public static boolean[] encode(double[][] bands, double f, int halfWindow) {
        if (bands == null) throw new IllegalArgumentException("bands == null");
        int B = bands.length;
        if (B == 0) throw new IllegalArgumentException("bands empty");
        int P = bands[0].length;

        boolean[] code = new boolean[B * P * 2];
        int idx = 0;

        for (int b = 0; b < B; b++) {
            if (bands[b].length != P) throw new IllegalArgumentException("ragged bands");

            double[] s = centerAndNormalize(bands[b]);

            for (int p = 0; p < P; p++) {
                Gabor1D.Complex c = Gabor1D.responseAt(s, p, halfWindow, f);

                code[idx++] = (c.re >= 0.0);
                code[idx++] = (c.im >= 0.0);
            }
        }

        return code;
    }

    private static double[] centerAndNormalize(double[] x) {
        int n = x.length;
        double mean = 0.0;
        for (double v : x) mean += v;
        mean /= n;

        double var = 0.0;
        for (double v : x) {
            double d = v - mean;
            var += d * d;
        }
        var /= n;
        double std = Math.sqrt(var);

        double[] out = new double[n];
        if (std < 1e-9) {
            // jak pas jest prawie stały, to tylko centruj
            for (int i = 0; i < n; i++) out[i] = x[i] - mean;
        } else {
            for (int i = 0; i < n; i++) out[i] = (x[i] - mean) / std;
        }
        return out;
    }
}