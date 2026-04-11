package biometria.iris;

import biometria.operations.morphology.StructuringElementShape;

public record MorphologyParams(int size, StructuringElementShape shape, int times) {
    public MorphologyParams {
        if (size <= 0 || size % 2 == 0) throw new IllegalArgumentException("size musi być nieparzyste i >0");
        if (times <= 0) throw new IllegalArgumentException("times musi być >0");
        if (shape == null) throw new IllegalArgumentException("shape == null");
    }
}