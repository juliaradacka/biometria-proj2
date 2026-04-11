package biometria.operations.morphology;

import biometria.model.ImageMatrix;
import biometria.operations.ImageOperation;
import biometria.operations.point.BinarizationOperation;

public class BinaryThenOperation implements ImageOperation {

    private final int threshold;
    private final ImageOperation op;

    public BinaryThenOperation(int threshold, ImageOperation op) {
        this.threshold = threshold;
        this.op = op;
    }

    @Override
    public ImageMatrix apply(ImageMatrix input) {
        ImageMatrix binary = new BinarizationOperation(threshold).apply(input);
        return op.apply(binary);
    }
}