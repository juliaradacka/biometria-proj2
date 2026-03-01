package biometria.gui;

import biometria.operations.ImageOperation;
import biometria.operations.filters.GrayScaleOperation;
import biometria.operations.filters.NegativeOperation;
import biometria.service.EditorService;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

import static biometria.gui.UIConstants.*;
import biometria.operations.filters.BrightnessOperation;
import biometria.gui.dialogs.BrightnessDialog;

public class MainFrame extends JFrame {

    private final EditorService editorService;
    private final ImagePanel imagePanel;
    private JFileChooser fileChooser;
    private JSplitPane splitPane;
    private JPanel sidePanel;


    public MainFrame(EditorService service) {
        this.editorService = service;
        this.imagePanel = new ImagePanel();

        setTitle("Biometria - Przetwarzanie obrazów");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

//        this.fileChooser = new JFileChooser();
//        FileNameExtensionFilter filter = new FileNameExtensionFilter(
//                "Obrazy (PNG, JPG, BMP)", "png", "jpg", "jpeg", "bmp");
//        fileChooser.setFileFilter(filter);
//        fileChooser.setAcceptAllFileFilterUsed(false);

        initComponents();
        initMenu();
    }

    private void initComponents() {
        // Panel boczny (na przyszłośc)
        sidePanel = new JPanel();
        sidePanel.setBackground(LIGHT_GREY);
        sidePanel.setPreferredSize(new Dimension(300,600));

        // ImagePanel
        JScrollPane imageScrollPane = new JScrollPane(imagePanel);
        imageScrollPane.setPreferredSize(new Dimension(800, 600));

        // SplitPane (dzieli okno na obraz i panel)
        splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                imageScrollPane,  // lewa strona - obraz
                sidePanel         // prawa strona - panel
        );
        splitPane.setDividerLocation(850);
        splitPane.setResizeWeight(0.75);
        splitPane.setOneTouchExpandable(true);

        add(splitPane, BorderLayout.CENTER);

        // File chooser
        fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Pliki obrazów (*.png, *.jpg, *.bmp, *.gif)",
                "png", "jpg", "jpeg", "bmp", "gif"
        );
        fileChooser.setFileFilter(filter);
        fileChooser.setAcceptAllFileFilterUsed(false);

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

        JMenu operationsMenu = new JMenu("Operacje");
        JMenuItem grayscaleItem = new JMenuItem("Skala szarości");
        grayscaleItem.addActionListener(e -> applyOperation(new GrayScaleOperation()));

        JMenuItem negativeItem = new JMenuItem("Negatyw");
        negativeItem.addActionListener(e -> applyOperation(new NegativeOperation()));

        JMenuItem brightnessItem = new JMenuItem("Jasność...");
        brightnessItem.addActionListener(e -> showBrightnessDialog());

        operationsMenu.add(grayscaleItem);
        operationsMenu.add(negativeItem);
        operationsMenu.add(brightnessItem);
        menuBar.add(operationsMenu);


        setJMenuBar(menuBar);
    }

    private void applyOperation(ImageOperation operation) {
        if (!editorService.hasImage()) {
            JOptionPane.showMessageDialog(this,
                    "Najpierw wczytaj obraz.",
                    "Brak obrazu",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        editorService.applyOperation(operation);
        refreshView();
    }

    private void showBrightnessDialog() {
        if (!editorService.hasImage()) {
            JOptionPane.showMessageDialog(this,
                    "Najpierw wczytaj obraz.",
                    "Brak obrazu",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        BrightnessDialog dialog = new BrightnessDialog(this);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            int value = dialog.getBrightnessValue();

            // Jeśli wartość = 0, nie rób nic
            if (value == 0) {
                return;
            }

            BrightnessOperation operation = new BrightnessOperation(value);
            editorService.applyOperation(operation);
            refreshView();
        }
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