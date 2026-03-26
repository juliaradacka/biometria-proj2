package biometria.operations.morphology;

import biometria.model.ImageMatrix;
import biometria.operations.ImageOperation;

public class ClosingOperation implements ImageOperation {
    @Override
    public ImageMatrix apply(ImageMatrix input) {

        DilatationOperation dilatationOperation = new DilatationOperation();
        ErosionOperation erosionOperation = new ErosionOperation();

        ImageMatrix result = dilatationOperation.apply(input);
        erosionOperation.apply(result);

        return result;
    }
}
