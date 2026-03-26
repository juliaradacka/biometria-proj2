package biometria.operations.morphology;

import biometria.model.ImageMatrix;
import biometria.operations.ImageOperation;

public class ClosingOperation implements ImageOperation {

    @Override
    public ImageMatrix apply(ImageMatrix input) {
        ImageOperation dilation = new DilatationOperation();
        ImageOperation erosion = new ErosionOperation();

        ImageMatrix result = dilation.apply(input);
        result = erosion.apply(result);
        return result;
    }
}