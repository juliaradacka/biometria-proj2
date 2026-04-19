package biometria.iris;

public final class Gabor1D {

    public static final class Complex {
        public final double re;
        public final double im;
        public Complex(double re, double im) { this.re = re; this.im = im; }
    }

    private Gabor1D() {}

    /**
     * g(n) = exp(-(n^2)/(2*sigma^2)) * exp(i*2*pi*f*n)
     * y(n0) = sum_k x[k] * g(k-n0)
     */
    public static Complex responseAt(double[] x, int n0, int halfWindow, double f) {
        // dla mniejszych f -> większe sigma (szersze okno)
        double sigma = Math.max(1.0, 0.5 * Math.PI / f);

        double re = 0.0;
        double im = 0.0;

        int nMin = Math.max(0, n0 - halfWindow);
        int nMax = Math.min(x.length - 1, n0 + halfWindow);

        for (int k = nMin; k <= nMax; k++) {
            double n = (k - n0);

            double gauss = Math.exp(-(n * n) / (2.0 * sigma * sigma));
            double ang = 2.0 * Math.PI * f * n;

            double cos = Math.cos(ang);
            double sin = Math.sin(ang);

            double val = x[k];

            re += val * gauss * cos;
            im += val * gauss * sin;
        }

        return new Complex(re, im);
    }
}