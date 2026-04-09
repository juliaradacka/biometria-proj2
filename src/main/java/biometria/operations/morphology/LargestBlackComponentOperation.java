package biometria.operations.morphology;

import biometria.model.ImageMatrix;
import biometria.operations.ImageOperation;
import biometria.util.ColorUtil;

import java.util.ArrayDeque;

public class LargestBlackComponentOperation implements ImageOperation {

    private static final int BLACK = 0;
    private static final int WHITE = 255;

    @Override
    public ImageMatrix apply(ImageMatrix input) {
        int w = input.getWidth();
        int h = input.getHeight();

        boolean[] visited = new boolean[w * h];

        int bestCount = 0;
        int[] bestPixels = null;

        int[] componentPixels = new int[w * h];

        ArrayDeque<Integer> q = new ArrayDeque<>();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int idx = y * w + x;
                if (visited[idx]) continue;

                int v = ColorUtil.getRed(input.getARGB(x, y));
                if (v != BLACK) {
                    visited[idx] = true;
                    continue;
                }

                // BFS po czarnych
                int count = 0;
                visited[idx] = true;
                q.add(idx);

                while (!q.isEmpty()) {
                    int cur = q.removeFirst();
                    componentPixels[count++] = cur;

                    int cx = cur % w;
                    int cy = cur / w;

                    // 4-sąsiedztwo
                    // lewo
                    if (cx > 0) tryPush(input, visited, q, w, cur - 1);
                    // prawo
                    if (cx + 1 < w) tryPush(input, visited, q, w, cur + 1);
                    // góra
                    if (cy > 0) tryPush(input, visited, q, w, cur - w);
                    // dół
                    if (cy + 1 < h) tryPush(input, visited, q, w, cur + w);
                }

                if (count > bestCount) {
                    bestCount = count;
                    bestPixels = new int[count];
                    System.arraycopy(componentPixels, 0, bestPixels, 0, count);
                }
            }
        }

        // jeśli nie ma czarnych pikseli, zwróć wejście
        if (bestPixels == null) return input;

        // budujemy wynik: wszystko białe, tylko największa składowa czarna
        ImageMatrix out = new ImageMatrix(w, h);
        int whiteArgb = ColorUtil.toARGB(255, WHITE, WHITE, WHITE);
        int blackArgb = ColorUtil.toARGB(255, BLACK, BLACK, BLACK);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                out.setARGB(x, y, whiteArgb);
            }
        }
        for (int p : bestPixels) {
            int x = p % w;
            int y = p / w;
            out.setARGB(x, y, blackArgb);
        }

        return out;
    }

    private static void tryPush(ImageMatrix input, boolean[] visited, ArrayDeque<Integer> q, int w, int idx) {
        if (visited[idx]) return;
        visited[idx] = true;

        int x = idx % w;
        int y = idx / w;

        int v = ColorUtil.getRed(input.getARGB(x, y));
        if (v == BLACK) {
            q.add(idx);
        }
    }
}