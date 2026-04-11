package biometria.operations.morphology;

public final class StructuringElementFactory {

    private StructuringElementFactory() {}

    // zwraca maskę SE jako boolean[size][size], gdzie true oznacza "należy do SE"
    public static boolean[][] create(int size, StructuringElementShape shape) {
        if (size <= 0) throw new IllegalArgumentException("rozmiar musi być > 0");
        if (size % 2 == 0) throw new IllegalArgumentException("rozmiar musi być nieparzysty (3,5,7,...)");

        boolean[][] se = new boolean[size][size];
        int c = size / 2;

        switch (shape) {
            case RECT -> {
                for (int y = 0; y < size; y++) {
                    for (int x = 0; x < size; x++) se[y][x] = true;
                }
            }
            case CROSS -> {
                for (int i = 0; i < size; i++) {
                    se[c][i] = true;
                    se[i][c] = true;
                }
            }
            case ELLIPSE -> { // koło wpisane w kwadrat
                int r2 = c * c;
                for (int y = 0; y < size; y++) {
                    for (int x = 0; x < size; x++) {
                        int dy = y - c;
                        int dx = x - c;
                        if (dx * dx + dy * dy <= r2) se[y][x] = true;
                    }
                }
            }
            case HORIZONTAL -> {
                for (int i = 0; i < size; i++) se[c][i] = true;
            }
            case VERTICAL -> {
                for (int i = 0; i < size; i++) se[i][c] = true;
            }
        }

        return se;
    }
}