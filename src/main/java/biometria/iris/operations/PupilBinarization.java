package biometria.iris.operations;

import biometria.iris.IrisAnalyzer;
import biometria.model.ImageMatrix;
import biometria.operations.ImageOperation;
import biometria.operations.point.BinarizationOperation;

public class PupilBinarization implements ImageOperation {

    private final double xp;

    public PupilBinarization(double xp) {
        if (xp <= 0.0) throw new IllegalArgumentException("xp must be > 0");
        this.xp = xp;
    }

    @Override
    public ImageMatrix apply(ImageMatrix img) {
        if (img == null) throw new IllegalArgumentException("img == null");
        double p = IrisAnalyzer.calculateAverageBrightness(img);
        int threshold = (int) Math.round(p / xp);
        threshold = Math.max(0, Math.min(255, threshold));
        return new BinarizationOperation(threshold).apply(img);
    }
}