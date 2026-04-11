package biometria.operations.morphology;

import biometria.model.ImageMatrix;
import biometria.operations.ImageOperation;

public class OpeningOperation implements ImageOperation {

    private final int size;
    private final StructuringElementShape shape;

    public OpeningOperation(int size, StructuringElementShape shape) {
        this.size = size;
        this.shape = shape;
    }

    @Override
    public ImageMatrix apply(ImageMatrix input) {
        ImageOperation erosion = new ErosionOperation(size, shape);
        ImageOperation dilation = new DilatationOperation(size, shape);

        ImageMatrix result = erosion.apply(input);
        result = dilation.apply(result);
        return result;
    }
}