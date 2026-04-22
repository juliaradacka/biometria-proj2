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

    private static int mod(int a, int m) {
        int r = a % m;
        return (r < 0) ? (r + m) : r;
    }
}