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
    private final ImagePanel unwrappedIrisPanel;
    private final FileHandler fileHandler;

    private SwingWorker<ImageMatrix, Void> previewWorker;
    private volatile int pendingPreviewValue;

    private JSplitPane splitPane;
    private JPanel sidePanel;

    private JPanel histogramContainer;
    private HistogramPanel currentHistogramPanel;

    private JPanel projectionsContainer;
    private JPanel projectionControls;

    private ProjectionPanel horizontalProjectionPanel;
    private ProjectionPanel verticalProjectionPanel;

    private JSpinner projectionThresholdSpinner;
    private JRadioButton objectDarkRadio;
    private JRadioButton objectBrightRadio;

    private static final int PROJECTION_THRESHOLD_MIN = 0;
    private static final int PROJECTION_THRESHOLD_MAX = 255;
    private static final int PROJECTION_THRESHOLD_DEFAULT = 128;

    public MainFrame(EditorService service) {
        this.editorService = service;
        this.unwrappedIrisPanel = new ImagePanel();
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
        sidePanel = new JPanel(new BorderLayout());
        sidePanel.setBackground(LIGHT_GRAY);
        sidePanel.setPreferredSize(new Dimension(300, 600));

//        histogramContainer = new JPanel(new BorderLayout());
//        histogramContainer.setBackground(LIGHT_GRAY);
//        histogramContainer.setBorder(BorderFactory.createTitledBorder("Histogram"));
//        histogramContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        projectionsContainer = new JPanel();
        projectionsContainer.setLayout(new BoxLayout(projectionsContainer, BoxLayout.Y_AXIS));
        projectionsContainer.setBackground(LIGHT_GRAY);
        projectionsContainer.setBorder(BorderFactory.createTitledBorder("Projekcje"));
        projectionsContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        projectionControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        projectionControls.setBackground(LIGHT_GRAY);
        projectionControls.setAlignmentX(Component.LEFT_ALIGNMENT);

        projectionThresholdSpinner = new JSpinner(
                new SpinnerNumberModel(PROJECTION_THRESHOLD_DEFAULT, PROJECTION_THRESHOLD_MIN, PROJECTION_THRESHOLD_MAX, 1)
        );

        objectDarkRadio = new JRadioButton("Ciemne", true);
        objectBrightRadio = new JRadioButton("Jasne", false);
        objectDarkRadio.setBackground(LIGHT_GRAY);
        objectBrightRadio.setBackground(LIGHT_GRAY);

        ButtonGroup bg = new ButtonGroup();
        bg.add(objectDarkRadio);
        bg.add(objectBrightRadio);

        projectionControls.add(new JLabel("Threshold:"));
        projectionControls.add(projectionThresholdSpinner);
        projectionControls.add(objectDarkRadio);
        projectionControls.add(objectBrightRadio);

        projectionThresholdSpinner.addChangeListener(e -> updateProjectionsPanel());
        objectDarkRadio.addActionListener(e -> updateProjectionsPanel());
        objectBrightRadio.addActionListener(e -> updateProjectionsPanel());

        JPanel rightTop = new JPanel(new BorderLayout(0,10));
//        rightTop.setLayout(new BoxLayout(rightTop, BoxLayout.Y_AXIS));
        rightTop.setBackground(LIGHT_GRAY);

        rightTop.add(createIrisDetectionPanel(), BorderLayout.NORTH);
        rightTop.add(Box.createVerticalStrut(10));
        rightTop.add(projectionsContainer, BorderLayout.CENTER);

        sidePanel.add(rightTop, BorderLayout.NORTH);

//        JScrollPane imageScrollPane = new JScrollPane(imagePanel);
//        imageScrollPane.setPreferredSize(new Dimension(800, 500));
        imagePanel.setPreferredSize(new Dimension(800,500));
        unwrappedIrisPanel.setPreferredSize(new Dimension(800, 150));

        JPanel bottomContainer = new JPanel(new BorderLayout());
        bottomContainer.setBorder(BorderFactory.createTitledBorder("Rozwinięcie biegunowe tęczówki"));
        bottomContainer.add(unwrappedIrisPanel, BorderLayout.CENTER);

        JPanel centerContainer = new JPanel(new BorderLayout(0, 5));
        centerContainer.add(imagePanel, BorderLayout.CENTER);
        centerContainer.add(bottomContainer, BorderLayout.SOUTH);

        splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                centerContainer,
                sidePanel
        );
        splitPane.setDividerLocation(850);
        splitPane.setResizeWeight(0.75);
        splitPane.setOneTouchExpandable(true);

        add(splitPane, BorderLayout.CENTER);

        updateProjectionsPanel();
    }

    private void initMenu() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(MenuFactory.createFileMenu(this));
        menuBar.add(MenuFactory.createEditMenu(this));
//        menuBar.add(MenuFactory.createOperationsMenu(this));
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
        refreshView();
    }

    void redo() {
        editorService.redo();
        refreshView();
    }

    void reset() {
        editorService.resetToOriginal();
        refreshView();
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

            final javax.swing.Timer localPreviewTimer =
                    new javax.swing.Timer(60, ev -> startPreviewWorker(operationFactory));
            localPreviewTimer.setRepeats(false);

            Integer value = ParameterDialog.showSliderDialog(
                    this,
                    name,
                    description,
                    min, max, initial,
                    (currentValue) -> {
                        pendingPreviewValue = currentValue;
                        localPreviewTimer.restart();
                    }
            );

            localPreviewTimer.stop();
            if (previewWorker != null && !previewWorker.isDone()) {
                previewWorker.cancel(true);
            }

            if (value != null) {
                applyOperation(operationFactory.apply(value));
            } else {
                refreshView();
            }
        });

        menu.add(item);
    }


    private void startPreviewWorker(Function<Integer, ImageOperation> operationFactory) {
        if (previewWorker != null && !previewWorker.isDone()) {
            previewWorker.cancel(true);
        }

        final int value = pendingPreviewValue;

        previewWorker = new SwingWorker<>() {
            @Override
            protected ImageMatrix doInBackground() {
                ImageMatrix base = editorService.getCurrent().copy(); // zawsze ostatni zatwierdzony stan
                return operationFactory.apply(value).apply(base);
            }

            @Override
            protected void done() {
                if (isCancelled()) return;

                try {
                    ImageMatrix preview = get();
                    imagePanel.setImage(preview);

                    if (currentHistogramPanel != null) {
                        currentHistogramPanel.updateHistogram(preview);
                    }

                    updateProjections(preview);

                } catch (Exception ex) {
                }
            }
        };

        previewWorker.execute();
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
        updateProjectionsPanel();
    }

    private void updateHistogram() {
        if (histogramContainer == null) return;

        ImageMatrix img = editorService.getCurrent();
        histogramContainer.removeAll();

        if (img != null) {
            currentHistogramPanel = new HistogramPanel(img);
            histogramContainer.add(currentHistogramPanel, BorderLayout.NORTH);
        } else {
            currentHistogramPanel = null;
        }

        histogramContainer.revalidate();
        histogramContainer.repaint();
    }

    private void updateProjectionsPanel() {
        if (projectionsContainer == null) return;

        projectionsContainer.removeAll();
        projectionsContainer.add(projectionControls);
        projectionsContainer.add(Box.createVerticalStrut(6));

        ImageMatrix img = editorService.getCurrent();
        if (img == null) {
            horizontalProjectionPanel = null;
            verticalProjectionPanel = null;
            projectionsContainer.revalidate();
            projectionsContainer.repaint();
            return;
        }

        int threshold = (Integer) projectionThresholdSpinner.getValue();
        boolean objectIsDark = objectDarkRadio.isSelected();

        int[] h = Projections.horizontal(img, threshold, objectIsDark);
        int[] v = Projections.vertical(img, threshold, objectIsDark);

        verticalProjectionPanel = new ProjectionPanel("Projekcja pionowa", true);
        horizontalProjectionPanel = new ProjectionPanel("Projekcja pozioma", false);

        verticalProjectionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        horizontalProjectionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        verticalProjectionPanel.updateProjection(v);
        horizontalProjectionPanel.updateProjection(h);

        projectionsContainer.add(verticalProjectionPanel);
        projectionsContainer.add(Box.createVerticalStrut(6));
        projectionsContainer.add(horizontalProjectionPanel);

        projectionsContainer.revalidate();
        projectionsContainer.repaint();
    }

    private void updateProjections(ImageMatrix image) {
        if (image == null) return;
        if (horizontalProjectionPanel == null || verticalProjectionPanel == null) return;

        int threshold = (Integer) projectionThresholdSpinner.getValue();
        boolean objectIsDark = objectDarkRadio.isSelected();

        horizontalProjectionPanel.updateProjection(Projections.horizontal(image, threshold, objectIsDark));
        verticalProjectionPanel.updateProjection(Projections.vertical(image, threshold, objectIsDark));
    }

    private JPanel createIrisDetectionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Proces segmentacji oka"));
        panel.setBackground(UIConstants.LIGHT_GRAY);

        JButton btnGray = new JButton("1. Przekształć do szarości");
        btnGray.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnGray.addActionListener(e -> {
            // TODO: przeksztalcenie do szarosci
        });

        JPanel pnlPupil = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlPupil.setBackground(UIConstants.LIGHT_GRAY);
        pnlPupil.add(new JLabel("X_P (Źrenica):"));

        JPanel pnlIris = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlIris.setBackground(UIConstants.LIGHT_GRAY);
        pnlIris.add(new JLabel("X_I (Tęczówka):"));


        SpinnerModel xpModel = new SpinnerNumberModel(2.0, 0.1, 10.0, 0.1);
        JSpinner spinnerXp = new JSpinner(xpModel);
        pnlPupil.add(spinnerXp);

        SpinnerModel xiModel = new SpinnerNumberModel(2.0, 0.1, 10.0, 0.1);
        JSpinner spinnerXi = new JSpinner(xiModel);
        pnlIris.add(spinnerXi);

        JButton btnBinPupil = new JButton("2. Binaryzacja Źrenicy");
        btnBinPupil.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnBinPupil.addActionListener(e -> {
            double xpValue = (Double) spinnerXp.getValue();
            // TODO: pobrać histogram, wyliczyć P / xpValue i zrobić binaryzację
        });

        JButton btnMorphPupil = new JButton("3. Morfologia");
        btnMorphPupil.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnMorphPupil.addActionListener(e -> {
            // TODO: wywołanie OpeningOperation / ClosingOperation
        });


        JButton btnFindCenter = new JButton("4. Znajdź środek i promień");
        btnFindCenter.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnFindCenter.addActionListener(e -> {
            // TODO: znajdywanie środka źrenicy
        });


        JButton btnUnwrap = new JButton("5. Rozwiń do prostokąta (Daugman)");
        btnUnwrap.setAlignmentX(Component.CENTER_ALIGNMENT);


        panel.add(Box.createVerticalStrut(5));
        panel.add(btnGray);
        panel.add(Box.createVerticalStrut(5));
        panel.add(pnlPupil);
        panel.add(pnlIris);
        panel.add(btnBinPupil);
        panel.add(Box.createVerticalStrut(5));
        panel.add(btnMorphPupil);
        panel.add(Box.createVerticalStrut(5));
        panel.add(btnFindCenter);
        panel.add(Box.createVerticalStrut(5));
        panel.add(btnUnwrap);
        panel.add(Box.createVerticalStrut(5));

        return panel;
    }
}