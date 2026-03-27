package biometria.operations.morphology;

public enum StructuringElementShape {
    RECT("rect"),
    CROSS("cross"),
    ELLIPSE("ellipse"),
    HORIZONTAL("horizontal"),
    VERTICAL("vertical");

    private final String id;

    StructuringElementShape(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static StructuringElementShape fromId(String id) {
        for (StructuringElementShape s : values()) {
            if (s.id.equalsIgnoreCase(id)) return s;
        }
        throw new IllegalArgumentException("Nieznany kształt: " + id);
    }
}