package biometria.io;

import biometria.model.ImageMatrix;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageIOService {

    public static ImageMatrix load(File file) throws IOException {
        if (file == null) {
            throw new IOException("Nie wybrano pliku");
        }
        if (!file.exists()) {
            throw new IOException("Plik nie istnieje: " + file.getAbsolutePath());
        }
        if (!file.isFile()) {
            throw new IOException("To nie jest plik: " + file.getName());
        }

        BufferedImage img;
        try {
            img = ImageIO.read(file);
        } catch (IOException e) {
            throw new IOException("Nie można odczytać pliku");
        }

        if (img == null) {
            String ext = getFileExtension(file.getName());
            throw new IOException(
                    "Nieobsługiwany format: " + ext.toUpperCase() +
                            "\n\nObsługiwane: PNG, JPG, JPEG, BMP"
            );
        }

        return ImageMatrix.fromBufferedImage(img);
    }

    private static String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }

    public static void save(ImageMatrix image, File file) throws IOException {
        String format = getFormatFromFileName(file.getName());

        boolean needsAlpha = !format.equals("jpg");
        BufferedImage img = image.toBufferedImage(needsAlpha);

        boolean result = ImageIO.write(img, format, file);
        if (!result) {
            throw new IOException("Błąd zapisu: nie odnaleziono encodera dla formatu " + format);
        }
    }

    private static String getFormatFromFileName(String name) {
        name = name.toLowerCase();
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "jpg";
        if (name.endsWith(".bmp")) return "bmp";
        return "png";
    }

    private ImageIOService() {
    }
}
