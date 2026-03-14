package biometria.gui;

import biometria.service.EditorService;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

public class FileHandler {

    private final JFileChooser fileChooser;
    private final EditorService editorService;
    private final Component parentComponent;

    public FileHandler(EditorService editorService, Component parentComponent) {
        this.editorService = editorService;
        this.parentComponent = parentComponent;
        this.fileChooser = createFileChooser();
    }

    public boolean openFile() {
        int result = fileChooser.showOpenDialog(parentComponent);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                editorService.loadImage(file);
                return true;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(parentComponent,
                        "Błąd wczytywania: " + ex.getMessage(),
                        "Błąd", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return false;
    }

    public boolean saveFile() {
        if (!editorService.hasImage()) {
            JOptionPane.showMessageDialog(parentComponent, "Brak obrazu do zapisania.");
            return false;
        }

        fileChooser.setSelectedFile(new File("wynik.png"));
        int result = fileChooser.showSaveDialog(parentComponent);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String path = file.getAbsolutePath();

            if (!hasValidExtension(path)) {
                if (file.getName().contains(".")) {
                    JOptionPane.showMessageDialog(parentComponent,
                            "Format pliku jest nieprawidłowy. Użyj .png, .jpg lub .bmp",
                            "Błędne rozszerzenie", JOptionPane.WARNING_MESSAGE);
                    return false;
                } else {
                    file = new File(path + ".png");
                }
            }

            try {
                editorService.saveImage(file);
                JOptionPane.showMessageDialog(parentComponent, "Zapisano: " + file.getName());
                return true;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(parentComponent,
                        "Błąd zapisu: " + ex.getMessage(),
                        "Błąd", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return false;
    }

    public String getLastOpenedFileName() {
        File selected = fileChooser.getSelectedFile();
        return selected != null ? selected.getName() : null;
    }

    private static JFileChooser createFileChooser() {
        JFileChooser chooser = new JFileChooser();

        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Obrazy (PNG, JPG, BMP)",
                "png", "jpg", "jpeg", "bmp"
        );
        chooser.setFileFilter(filter);
        chooser.setAcceptAllFileFilterUsed(false);

        return chooser;
    }

    private boolean hasValidExtension(String path) {
        String p = path.toLowerCase();
        return p.endsWith(".png") || p.endsWith(".jpg") ||
                p.endsWith(".jpeg") || p.endsWith(".bmp");
    }
}