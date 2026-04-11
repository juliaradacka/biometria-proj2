package biometria.iris.operations;

import biometria.iris.IrisAnalyzer;
import biometria.model.ImageMatrix;
import biometria.operations.ImageOperation;
import biometria.operations.point.BinarizationOperation;

public class IrisBinarization implements ImageOperation {

    private final double xi;

    public IrisBinarization(double xi) {
        if (xi <= 0.0) throw new IllegalArgumentException("xi must be > 0");
        this.xi = xi;
    }

    @Override
    public ImageMatrix apply(ImageMatrix img) {
        if (img == null) throw new IllegalArgumentException("img == null");
        double p = IrisAnalyzer.calculateAverageBrightness(img);
        int threshold = (int) Math.round(p / xi);
        threshold = Math.max(0, Math.min(255, threshold));
        return new BinarizationOperation(threshold).apply(img);
    }
}