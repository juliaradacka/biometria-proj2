package biometria.operations.morphology;

import biometria.model.ImageMatrix;
import biometria.operations.ImageOperation;
import biometria.operations.point.BinarizationOperation;

public class OpeningOperation implements ImageOperation {
    @Override
    public ImageMatrix apply(ImageMatrix input) {
        ErosionOperation erosionOperation = new ErosionOperation();
        DilatationOperation dilatationOperation = new DilatationOperation();


        ImageMatrix result = erosionOperation.apply(input);
        dilatationOperation.apply(result);

        return result;
    }
}
