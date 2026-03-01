package biometria.model;

import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Graphics2D;

public class ImageMatrix {
    private final int width;
    private final int height;
    private final int[] argb;

    public ImageMatrix(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Szerokość i wysokość muszą być nieujemne: szerokość=" + width + ", wysokość=" + height);
        }
        this.width = width;
        this.height = height;
        this.argb = new int[width * height];
    }

    public ImageMatrix(int width, int height, int[] argb) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Szerokość i wysokość muszą być nieujemne: szerokość=" + width + ", wysokość=" + height);
        }
        if (argb == null) {
            throw new IllegalArgumentException("Tablica argb nie może być pusta");
        }
        if (argb.length != width * height) {
            throw new IllegalArgumentException("Długość tablicy argb (" + argb.length + ") nie jest zgodna z width*height (" + (width * height) + ")");
        }
        this.width = width;
        this.height = height;
        this.argb = argb;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getARGB(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new IndexOutOfBoundsException(
                    "Współrzędne (" + x + ", " + y + ") wykraczają poza granice" +
                            "(" + width + "x" + height + ")"
            );
        }
        return argb[y * width + x];
    }

    public void setARGB(int x, int y, int value) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new IndexOutOfBoundsException(
                    "Współrzędne (" + x + ", " + y + ") wykraczają poza granice" +
                            "(" + width + "x" + height + ")"
            );
        }
        argb[y * width + x] = value;
    }

    public int getARGBClamped(int x, int y) {
        x = Math.max(0, Math.min(x, width - 1));
        y = Math.max(0, Math.min(y, height - 1));
        return argb[y * width + x];
    }

    public ImageMatrix copy() {
        return new ImageMatrix(width, height, argb.clone());
    }

    public static ImageMatrix fromBufferedImage(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        int[] pixels = new int[w * h];
        img.getRGB(0, 0, w, h, pixels, 0, w);
        return new ImageMatrix(w, h, pixels);
    }

    public BufferedImage toBufferedImage(boolean includeAlpha) {
        int type = includeAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        BufferedImage img = new BufferedImage(width, height, type);

        if (!includeAlpha) {
            Graphics2D g = img.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);
            g.dispose();
            img.setRGB(0, 0, width, height, argb, 0, width);
        } else {
            img.setRGB(0, 0, width, height, argb, 0, width);
        }

        return img;
    }
}
