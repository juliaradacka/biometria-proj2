package biometria.operations;

import biometria.model.ImageMatrix;

@FunctionalInterface
public interface ImageOperation {
    ImageMatrix apply(ImageMatrix input);
}