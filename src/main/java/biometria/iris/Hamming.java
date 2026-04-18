package biometria.iris;

public final class Hamming {

    private Hamming() {}

    public static double distance(boolean[] a, boolean[] b) {
        if (a == null || b == null) throw new IllegalArgumentException("null code");
        if (a.length != b.length) throw new IllegalArgumentException("length mismatch");

        int diff = 0;
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) diff++;
        }
        return (double) diff / a.length;
    }

    /**
     * minimalna odległość Hamminga z przesunięciem kątowym
     * - mamy 2 bity na punkt więc przesuwamy blokami po 2 bity
     */
    public static double minDistanceWithShift(boolean[] codeA, boolean[] codeB, int bands, int points, int maxShift) {
        int expected = bands * points * 2;
        double best = Double.POSITIVE_INFINITY;

        for (int shift = -maxShift; shift <= maxShift; shift++) {
            int diff = 0;

            for (int b = 0; b < bands; b++) {
                for (int p = 0; p < points; p++) {
                    int p2 = mod(p + shift, points);

                    int baseA = (b * points + p) * 2;
                    int baseB = (b * points + p2) * 2;

                    if (codeA[baseA] != codeB[baseB]) diff++;
                    if (codeA[baseA + 1] != codeB[baseB + 1]) diff++;
                }
            }

            double d = (double) diff / expected;
            if (d < best) best = d;
        }

        return best;
    }

    private static int mod(int a, int m) {
        int r = a % m;
        return (r < 0) ? (r + m) : r;
    }
}