package biometria.operations.morphology;

import biometria.model.ImageMatrix;
import biometria.operations.ImageOperation;

public class ClosingOperation implements ImageOperation {

    private final int size;
    private final StructuringElementShape shape;

    public ClosingOperation(int size, StructuringElementShape shape) {
        this.size = size;
        this.shape = shape;
    }

    @Override
    public ImageMatrix apply(ImageMatrix input) {
        ImageOperation dilation = new DilatationOperation(size, shape);
        ImageOperation erosion = new ErosionOperation(size, shape);

        ImageMatrix result = dilation.apply(input);
        result = erosion.apply(result);
        return result;
    }
}