package biometria.gui;

import biometria.service.EditorService;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

public class MainFrame extends JFrame {

    private final EditorService editorService;
    private final ImagePanel imagePanel;
    private final JFileChooser fileChooser;

    public MainFrame() {
        super("Biometria - Przetwarzanie obrazów");
        this.editorService = new EditorService();
        this.imagePanel = new ImagePanel();
        this.fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Obrazy (PNG, JPG, BMP)", "png", "jpg", "jpeg", "bmp");
        fileChooser.setFileFilter(filter);
        fileChooser.setAcceptAllFileFilterUsed(false);

        initLayout();
        initMenu();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
    }

    private void initLayout() {
        setLayout(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(imagePanel);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void initMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("Plik");

        JMenuItem openItem = new JMenuItem("Otwórz plik");
        openItem.addActionListener(e -> openFile());
        fileMenu.add(openItem);

        JMenuItem saveItem = new JMenuItem("Zapisz jako");
        saveItem.addActionListener(e -> saveFile());
        fileMenu.add(saveItem);

        fileMenu.addSeparator();

        JMenuItem exitItem = new JMenuItem("Zamknij program");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        menuBar.add(fileMenu);

        // --- Menu Edycja ---
        JMenu editMenu = new JMenu("Edycja");

        JMenuItem undoItem = new JMenuItem("Cofnij");
        undoItem.addActionListener(e -> {
            editorService.undo();
            refreshView();
        });
        editMenu.add(undoItem);

        JMenuItem redoItem = new JMenuItem("Ponów");
        redoItem.addActionListener(e -> {
            editorService.redo();
            refreshView();
        });
        editMenu.add(redoItem);

        editMenu.addSeparator();

        JMenuItem resetItem = new JMenuItem("Resetuj do oryginału");
        resetItem.addActionListener(e -> {
            editorService.resetToOriginal();
            refreshView();
        });
        editMenu.add(resetItem);

        menuBar.add(editMenu);

        setJMenuBar(menuBar);
    }

    private void openFile() {
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                editorService.loadImage(file);
                refreshView();
                setTitle("Biometria - " + file.getName());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Błąd wczytywania: " + ex.getMessage(),
                        "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveFile() {
        if (!editorService.hasImage()) {
            JOptionPane.showMessageDialog(this, "Brak obrazu do zapisania.");
            return;
        }
        fileChooser.setSelectedFile(new File("wynik.png"));
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String path = file.getAbsolutePath();

            if (!hasValidExtension(path)) {
                if (file.getName().contains(".")) {
                    JOptionPane.showMessageDialog(this,
                            "Format pliku jest nieprawidłowy. Użyj .png, .jpg lub .bmp",
                            "Błędne rozszerzenie", JOptionPane.WARNING_MESSAGE);
                    return;
                } else {
                    file = new File(path + ".png");
                }
            }
            try {
                editorService.saveImage(file);
                JOptionPane.showMessageDialog(this, "Zapisano: " + file.getName());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Błąd zapisu: " + ex.getMessage(),
                        "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean hasValidExtension(String path) {
        String p = path.toLowerCase();
        return p.endsWith(".png") || p.endsWith(".jpg") || p.endsWith(".jpeg") || p.endsWith(".bmp");
    }

    private void refreshView() {
        imagePanel.setImage(editorService.getCurrent());
    }

}