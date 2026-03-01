package biometria.io;

import biometria.model.ImageMatrix;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Images {

    public static ImageMatrix loadImage(File file) throws IOException {
        if (file == null) {
            throw new IOException("Nie wybrano pliku");
        }
        if (!file.exists()) {
            throw new IOException("Plik nie istnieje: " + file.getAbsolutePath());
        }
        if (!file.isFile()) {
            throw new IOException("To nie jest plik: " + file.getName());
        }

        BufferedImage img = ImageIO.read(file);

        if (img == null) {
            throw new IOException("Nie można odczytać obrazu: " + file.getName());
        }

        return ImageMatrix.fromBufferedImage(img);
    }

    public static void saveImage(ImageMatrix image, File file) throws IOException {
        BufferedImage buffered = image.toBufferedImage(true);
        String format = determineFormat(file);

        boolean success = ImageIO.write(buffered, format, file);

        if (!success) {
            throw new IOException("Nie można zapisać obrazu w formacie: " + format);
        }
    }

    private static String determineFormat(File file) {
        String fileName = file.getName().toLowerCase();

        if (fileName.endsWith(".png")) {
            return "png";
        }
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "jpg";
        }
        if (fileName.endsWith(".bmp")) {
            return "bmp";
        }

        return "png";
    }

    private Images() {
        throw new AssertionError("Utility class");
    }
}
