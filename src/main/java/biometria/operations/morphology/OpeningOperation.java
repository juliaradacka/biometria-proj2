package biometria.operations.morphology;

import biometria.model.ImageMatrix;
import biometria.operations.ImageOperation;

public class OpeningOperation implements ImageOperation {

    @Override
    public ImageMatrix apply(ImageMatrix input) {
        ImageOperation erosion = new ErosionOperation();
        ImageOperation dilation = new DilatationOperation();

        ImageMatrix result = erosion.apply(input);
        result = dilation.apply(result);
        return result;
    }
}