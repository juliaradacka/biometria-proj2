package biometria.gui;

import biometria.operations.ImageOperation;
import biometria.operations.point.BrightnessOperation;
import biometria.operations.point.ContrastOperation;
import biometria.operations.point.GrayScaleOperation;
import biometria.operations.point.NegativeOperation;
import biometria.service.EditorService;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

import static biometria.gui.UIConstants.*;

public class MainFrame extends JFrame {

    private final EditorService editorService;
    private final ImagePanel imagePanel;
    private final JFileChooser fileChooser;
    private JSplitPane splitPane;
    private JPanel sidePanel;


    public MainFrame(EditorService service) {
        this.editorService = service;
        this.imagePanel = new ImagePanel();
        this.fileChooser = createFileChooser();

        setTitle("Biometria - Przetwarzanie obrazów");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        initComponents();
        initMenu();
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

    private void initComponents() {
        // TO DO: panel na histogram i statystyki (na ocenę 4.0)
        sidePanel = new JPanel();
        sidePanel.setBackground(LIGHT_GRAY);
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
    }

    private void initMenu() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createFileMenu());
        menuBar.add(createEditMenu());
        menuBar.add(createOperationsMenu());
        setJMenuBar(menuBar);
    }

    private JMenu createFileMenu() {
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

        return fileMenu;
    }

    private JMenu createEditMenu() {
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

        return editMenu;
    }

    private JMenu createOperationsMenu() {
        JMenu operationsMenu = new JMenu("Operacje");

        JMenuItem grayScaleItem = new JMenuItem("Odcienie szarości");
        grayScaleItem.addActionListener(e -> applyOperation(new GrayScaleOperation()));

        JMenuItem negativeItem = new JMenuItem("Negatyw");
        negativeItem.addActionListener(e -> applyOperation(new NegativeOperation()));

        operationsMenu.add(grayScaleItem);
        operationsMenu.add(negativeItem);

        JMenuItem brightnessItem = new JMenuItem("Korekta jasności");
        brightnessItem.addActionListener(e -> {
            if (!validateImageLoaded()) return;

            Integer value = ParameterDialog.showSliderDialog(
                    this,
                    "Korekta jasności",
                    "Dostosuj jasność obrazu",
                    -255, 255, 0
            );

            if (value != null) {
                applyOperation(new BrightnessOperation(value));
            }
        });
        operationsMenu.add(brightnessItem);


        JMenuItem contrastItem = new JMenuItem("Korekta kontrastu");
        contrastItem.addActionListener(e-> {
            if(!validateImageLoaded()) return;

            String input = JOptionPane.showInputDialog(this,"Podaj wartość zmiany kontrastu (-255 do 255):","0");
            if(input!= null){
                try{
                    int offset = Integer.parseInt(input);
                    applyOperation(new ContrastOperation(offset));
                } catch (NumberFormatException ex){
                    JOptionPane.showMessageDialog(this, "Proszę podać prawidłową liczbę całkowitą.");
                }
            }
        });

        operationsMenu.add(contrastItem);

        return operationsMenu;
    }

    private boolean validateImageLoaded() {
        if (!editorService.hasImage()) {
            JOptionPane.showMessageDialog(this,
                    "Najpierw wczytaj obraz.",
                    "Brak obrazu",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void applyOperation(ImageOperation operation) {
        if (!validateImageLoaded()) return;
        editorService.applyOperation(operation);
        refreshView();
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