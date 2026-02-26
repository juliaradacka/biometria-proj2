package biometria.io;

import biometria.model.ImageMatrix;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageIOService {

    public static ImageMatrix load(File file) throws IOException {
        BufferedImage img = ImageIO.read(file);
        if (img == null) {
            throw new IOException("Nieobsługiwany format pliku: " + file.getName());
        }
        return ImageMatrix.fromBufferedImage(img);
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
