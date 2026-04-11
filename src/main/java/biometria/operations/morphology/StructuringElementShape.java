package biometria.operations.morphology;

public enum StructuringElementShape {
    RECT("Kwadrat"),
    CROSS("Krzyżyk"),
    ELLIPSE("Koło (wpisane)"),
    HORIZONTAL("Linia pozioma"),
    VERTICAL("Linia pionowa");

    private final String displayName;

    StructuringElementShape(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}