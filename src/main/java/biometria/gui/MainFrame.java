package biometria.gui;

import biometria.iris.IrisSegmentationState;
import biometria.iris.operations.IrisBinarization;
import biometria.iris.operations.PupilBinarization;
import biometria.iris.operations.PupilMaskCleanupOperation;
import biometria.model.ImageMatrix;
import biometria.operations.ImageOperation;
import biometria.operations.point.grayscale.GrayScaleAverageOperation;
import biometria.service.EditorService;

import javax.swing.*;
import java.awt.*;

import static biometria.gui.UIConstants.*;

public class MainFrame extends JFrame {

    private final EditorService editorService;
    private final ImagePanel imagePanel;
    private final ImagePanel unwrappedIrisPanel;
    private final FileHandler fileHandler;

    private final IrisSegmentationState irisState = new IrisSegmentationState();

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

        JPanel rightTop = new JPanel(new BorderLayout(0, 10));
        rightTop.setBackground(LIGHT_GRAY);

        rightTop.add(new IrisSegmentationPanel(
                // 1) grayscale baza (przez applyOperation)
                () -> {
                    if (!validateImageLoaded()) return;

                    applyOperation(new GrayScaleAverageOperation());

                    irisState.setBaseGray(editorService.getCurrent().copy());
                    irisState.setPupilMask(null);
                    irisState.setIrisMask(null);
                },

                // 2a) pupil mask
                (xp) -> {
                    if (!validateImageLoaded()) return;
                    if (irisState.getBaseGray() == null) {
                        JOptionPane.showMessageDialog(this, "Najpierw krok 1 (baza szarości).");
                        return;
                    }

                    ImageMatrix mask = new PupilBinarization(xp).apply(irisState.getBaseGray().copy());
                    irisState.setPupilMask(mask);

                    imagePanel.setImage(mask);
                    updateProjections(mask);
                },

                // 2b) iris mask
                (xi) -> {
                    if (!validateImageLoaded()) return;
                    if (irisState.getBaseGray() == null) {
                        JOptionPane.showMessageDialog(this, "Najpierw krok 1 (baza szarości).");
                        return;
                    }

                    ImageMatrix mask = new IrisBinarization(xi).apply(irisState.getBaseGray().copy());
                    irisState.setIrisMask(mask);

                    imagePanel.setImage(mask);
                    updateProjections(mask);
                },

                // 3a) morfologia źrenicy (cleanup)
                () -> {
                    if (irisState.getPupilMask() == null) {
                        JOptionPane.showMessageDialog(this, "Najpierw policz maskę źrenicy (2a).");
                        return;
                    }

                    ImageMatrix cleaned = new PupilMaskCleanupOperation()
                            .apply(irisState.getPupilMask());

                    irisState.setPupilMask(cleaned);
                    imagePanel.setImage(cleaned);
                    updateProjections(cleaned);
                }
        ), BorderLayout.NORTH);

        rightTop.add(Box.createVerticalStrut(10));
        rightTop.add(projectionsContainer, BorderLayout.CENTER);

        sidePanel.add(rightTop, BorderLayout.NORTH);

        imagePanel.setPreferredSize(new Dimension(800, 500));
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
        setJMenuBar(menuBar);
    }

    void openFile() {
        if (fileHandler.openFile()) {
            refreshView();

            // po wczytaniu nowego pliku czyścimy stan segmentacji
            irisState.setBaseGray(null);
            irisState.setPupilMask(null);
            irisState.setIrisMask(null);

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
        irisState.setBaseGray(null);
        irisState.setPupilMask(null);
        irisState.setIrisMask(null);
    }

    void redo() {
        editorService.redo();
        refreshView();
        irisState.setBaseGray(null);
        irisState.setPupilMask(null);
        irisState.setIrisMask(null);
    }

    void reset() {
        editorService.resetToOriginal();
        refreshView();
        irisState.setBaseGray(null);
        irisState.setPupilMask(null);
        irisState.setIrisMask(null);
    }

    void applyOperation(ImageOperation operation) {
        if (!validateImageLoaded()) return;
        editorService.applyOperation(operation);
        refreshView();
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
}