package biometria;

import biometria.gui.MainFrame;
import biometria.service.EditorService;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            EditorService service = new EditorService();
            MainFrame frame = new MainFrame(service);
            frame.setVisible(true);
        });
    }
}