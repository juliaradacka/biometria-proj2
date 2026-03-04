package biometria.gui;

import biometria.model.ImageMatrix;
import biometria.operations.ImageOperation;
import biometria.operations.filter.ConvolutionOperation;
import biometria.operations.point.*;
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
    private JPanel histogramContainer;
    private HistogramPanel currentHistogramPanel;


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
        sidePanel = new JPanel();
        sidePanel.setLayout(new BorderLayout());
        sidePanel.setBackground(LIGHT_GRAY);
        sidePanel.setPreferredSize(new Dimension(300,600));

        histogramContainer = new JPanel(new BorderLayout());
        histogramContainer.setBackground(LIGHT_GRAY);
        histogramContainer.setBorder(BorderFactory.createTitledBorder("Histogram"));

        sidePanel.add(histogramContainer,BorderLayout.NORTH);

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

            ImageMatrix originalForPreview = editorService.getCurrent().copy();

            Integer value = ParameterDialog.showSliderDialog(
                    this,
                    "Korekta jasności",
                    "Dostosuj jasność obrazu",
                    -255, 255, 0,
                    (currentValue) -> {
                        ImageMatrix preview = new BrightnessOperation(currentValue).apply(originalForPreview);
                        imagePanel.setImage(preview);
                        if (currentHistogramPanel != null) {
                            currentHistogramPanel.updateHistogram(preview);
                        }
                    }

            );

            if (value != null) {
                imagePanel.setImage(originalForPreview);
                applyOperation(new BrightnessOperation(value));
            } else{
                imagePanel.setImage(originalForPreview);
            }
        });
        operationsMenu.add(brightnessItem);


        JMenuItem contrastItem = new JMenuItem("Korekta kontrastu");
        contrastItem.addActionListener(e -> {
            if (!validateImageLoaded()) return;

            ImageMatrix originalForPreview = editorService.getCurrent().copy();

            Integer value = ParameterDialog.showSliderDialog(
                    this,
                    "Korekta kontrastu",
                    "Dostosuj kontrast obrazu",
                    -255, 255, 0,
                    (currentValue) -> {
                        ImageMatrix preview = new ContrastOperation(currentValue).apply(originalForPreview);
                        imagePanel.setImage(preview);
                        if (currentHistogramPanel != null) {
                            currentHistogramPanel.updateHistogram(preview);
                        }
                    }
            );

            if (value != null) {
                imagePanel.setImage(originalForPreview);
                applyOperation(new ContrastOperation(value));
            }
            else{
                imagePanel.setImage(originalForPreview);
            }
        });
        operationsMenu.add(contrastItem);


        JMenuItem binerazationItem = new JMenuItem("Binaryzacja");
        binerazationItem.addActionListener(e -> {
            if (!validateImageLoaded()) return;

            ImageMatrix originalForPreview = editorService.getCurrent().copy();

            Integer value = ParameterDialog.showSliderDialog(
                    this,
                    "Binaryzacja",
                    "Ustaw próg (threshold) bieli i czerni:",
                    0, 255, 128,
                    (currentValue) -> {
                        ImageMatrix preview = new BinarizationOperation(currentValue).apply(originalForPreview);
                        imagePanel.setImage(preview);
                        if (currentHistogramPanel != null) {
                            currentHistogramPanel.updateHistogram(preview);
                        }
                    }

            );

            if (value != null) {
                imagePanel.setImage(originalForPreview);
                applyOperation(new BinarizationOperation(value));
            }
            else{
                imagePanel.setImage(originalForPreview);
            }
        });
        operationsMenu.add(binerazationItem);

        JMenu convolutionMenu = new JMenu("Filtry Splotowe");
        JMenuItem averageItem = new JMenuItem("Filtr Uśredniający");
        averageItem.addActionListener(e -> {
            if(!validateImageLoaded()) return;
            double[][] mask = {
                    {1, 1, 1},
                    {1, 1, 1},
                    {1, 1, 1}
            };
            applyOperation(new ConvolutionOperation(mask, 9.0));
        });
        convolutionMenu.add(averageItem);

        JMenuItem gaussianItem = new JMenuItem("Filtr Gaussa");
        gaussianItem.addActionListener(e -> {
            if(!validateImageLoaded()) return;
            double[][] mask = {
                    {1, 2, 1},
                    {2, 4, 2},
                    {1, 2, 1}
            };
            applyOperation(new ConvolutionOperation(mask, 16.0));
        });
        convolutionMenu.add(gaussianItem);

        JMenuItem sharpenItem = new JMenuItem("Wyostrzanie");
        sharpenItem.addActionListener(e -> {
            if (!validateImageLoaded()) return;
            double[][] mask = {
                    { 0, -1,  0},
                    {-1,  5, -1},
                    { 0, -1,  0}
            };
            applyOperation(new ConvolutionOperation(mask, 1.0));
        });
        convolutionMenu.add(sharpenItem);


        operationsMenu.addSeparator();
        operationsMenu.add(convolutionMenu);

        updateHistogram();

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

        updateHistogram();
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
        updateHistogram();
    }

    private void updateHistogram() {
        if (histogramContainer == null || editorService.getCurrent() == null) return;

        histogramContainer.removeAll();

        currentHistogramPanel = new HistogramPanel(editorService.getCurrent());

        histogramContainer.add(currentHistogramPanel, BorderLayout.NORTH);
        histogramContainer.revalidate();
        histogramContainer.repaint();
    }

}