package biometria.gui;

import biometria.model.ImageMatrix;
import biometria.operations.ImageOperation;
import biometria.service.EditorService;

import javax.swing.*;
import java.awt.*;
import java.util.function.Function;

import static biometria.gui.UIConstants.*;

public class MainFrame extends JFrame {

    private final EditorService editorService;
    private final ImagePanel imagePanel;
    private final FileHandler fileHandler;

    private JSplitPane splitPane;
    private JPanel sidePanel;
    private JPanel histogramContainer;
    private HistogramPanel currentHistogramPanel;

    public MainFrame(EditorService service) {
        this.editorService = service;
        this.imagePanel = new ImagePanel();
        this.fileHandler = new FileHandler(editorService, this);

        setTitle("Biometria - Przetwarzanie obrazów");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        initComponents();
        initMenu();
    }

    private void initComponents() {
        sidePanel = new JPanel();
        sidePanel.setLayout(new BorderLayout());
        sidePanel.setBackground(LIGHT_GRAY);
        sidePanel.setPreferredSize(new Dimension(300, 600));

        histogramContainer = new JPanel(new BorderLayout());
        histogramContainer.setBackground(LIGHT_GRAY);
        histogramContainer.setBorder(BorderFactory.createTitledBorder("Histogram"));

        sidePanel.add(histogramContainer, BorderLayout.NORTH);

        JScrollPane imageScrollPane = new JScrollPane(imagePanel);
        imageScrollPane.setPreferredSize(new Dimension(800, 600));

        splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                imageScrollPane,
                sidePanel
        );
        splitPane.setDividerLocation(850);
        splitPane.setResizeWeight(0.75);
        splitPane.setOneTouchExpandable(true);

        add(splitPane, BorderLayout.CENTER);
    }

    private void initMenu() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(MenuFactory.createFileMenu(this));
        menuBar.add(MenuFactory.createEditMenu(this));
        menuBar.add(MenuFactory.createOperationsMenu(this));
        setJMenuBar(menuBar);
    }

    void openFile() {
        if (fileHandler.openFile()) {
            refreshView();
            String fileName = fileHandler.getLastOpenedFileName();
            if (fileName != null) {
                setTitle("Biometria - " + fileName);
            }
        }
    }

    void saveFile() {
        fileHandler.saveFile();
    }

    void undo() {
        editorService.undo();
    }

    void redo() {
        editorService.redo();
    }

    void reset() {
        editorService.resetToOriginal();
    }

    void applyOperation(ImageOperation operation) {
        if (!validateImageLoaded()) return;
        editorService.applyOperation(operation);
        refreshView();
    }

    void createParametricOperationItem(
            JMenu menu,
            String name,
            String description,
            int min,
            int max,
            int initial,
            Function<Integer, ImageOperation> operationFactory
    ) {
        JMenuItem item = new JMenuItem(name);
        item.addActionListener(e -> {
            if (!validateImageLoaded()) return;

            ImageMatrix originalForPreview = editorService.getCurrent().copy();

            Integer value = ParameterDialog.showSliderDialog(
                    this,
                    name,
                    description,
                    min, max, initial,
                    (currentValue) -> {
                        ImageMatrix preview = operationFactory.apply(currentValue)
                                .apply(originalForPreview);
                        imagePanel.setImage(preview);

                        if (currentHistogramPanel != null) {
                            currentHistogramPanel.updateHistogram(preview);
                        }
                    }
            );

            if (value != null) {
                imagePanel.setImage(originalForPreview);
                applyOperation(operationFactory.apply(value));
            } else {
                imagePanel.setImage(originalForPreview);
            }
        });

        menu.add(item);
    }

    boolean validateImageLoaded() {
        if (!editorService.hasImage()) {
            JOptionPane.showMessageDialog(this,
                    "Najpierw wczytaj obraz.",
                    "Brak obrazu",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    void refreshView() {
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