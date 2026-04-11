package biometria.operations.morphology;

import biometria.model.ImageMatrix;
import biometria.operations.ImageOperation;

public class RepeatOperation implements ImageOperation {

    private final ImageOperation op;
    private final int times;

    public RepeatOperation(ImageOperation op, int times) {
        this.op = op;
        this.times = Math.max(1, times);
    }

    @Override
    public ImageMatrix apply(ImageMatrix input) {
        ImageMatrix result = input;
        for (int i = 0; i < times; i++) {
            result = op.apply(result);
        }
        return result;
    }
}