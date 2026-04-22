package biometria.gui;

import biometria.iris.IrisSegmentationState;
import biometria.iris.PupilCenterEstimator;
import biometria.iris.operations.IrisBinarization;
import biometria.iris.operations.IrisMaskCleanupOperation;
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

    private JSplitPane splitPane;
    private JPanel sidePanel;
    private JCheckBox showPupilCircleCheckBox;
    private JCheckBox showIrisCircleCheckBox;
    private JCheckBox showIrisCodeCheckBox;

    private boolean[] lastCode = null;
    private String lastFileName = null;

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

        JPanel overlayControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        overlayControls.setBackground(LIGHT_GRAY);
        overlayControls.setBorder(BorderFactory.createTitledBorder("Overlay"));

        showPupilCircleCheckBox = new JCheckBox("Pokaż granice źrenicy");
        showPupilCircleCheckBox.setBackground(LIGHT_GRAY);
        showPupilCircleCheckBox.addActionListener(e -> updatePupilOverlay());
        overlayControls.add(showPupilCircleCheckBox);

        showIrisCircleCheckBox = new JCheckBox("Pokaż granice tęczówki");
        showIrisCircleCheckBox.setBackground(LIGHT_GRAY);
        showIrisCircleCheckBox.addActionListener(e -> updateIrisOverlay());
        overlayControls.add(showIrisCircleCheckBox);

        JPanel rightTop = new JPanel(new BorderLayout(0, 10));
        rightTop.setBackground(LIGHT_GRAY);

        rightTop.add(new IrisSegmentationPanel(
                // 1) grayscale baza
                () -> {
                    if (!validateImageLoaded()) return;
                    clearSegmentationAndOverlays();
                    applyOperation(new GrayScaleAverageOperation());
                    irisState.setBaseGray(editorService.getCurrent().copy());
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
                    updatePupilOverlay();
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
                    updateIrisOverlay();
                },

                // 3a) morfologia źrenicy
                () -> {
                    if (irisState.getPupilMask() == null) {
                        JOptionPane.showMessageDialog(this, "Najpierw policz maskę źrenicy (2a).");
                        return;
                    }

                    ImageMatrix cleaned = new PupilMaskCleanupOperation().apply(irisState.getPupilMask());
                    irisState.setPupilMask(cleaned);

                    imagePanel.setImage(cleaned);
                    updatePupilOverlay();
                },

                // 3b) morfologia tęczówki
                () -> {
                    if (irisState.getIrisMask() == null) {
                        JOptionPane.showMessageDialog(this, "Najpierw policz maskę tęczówki (2b).");
                        return;
                    }

                    ImageMatrix cleaned = new IrisMaskCleanupOperation().apply(irisState.getIrisMask());
                    irisState.setIrisMask(cleaned);

                    imagePanel.setImage(cleaned);
                    updateIrisOverlay();
                },

                // 5) rozwinięcie Daugmana
                () -> {
                    if (irisState.getPupilMask() == null || irisState.getIrisMask() == null) {
                        JOptionPane.showMessageDialog(this, "Musisz wyliczyć maski źrenicy (3a) i tęczówki (3b)!");
                        return;
                    }

                    double[] pupilEst = PupilCenterEstimator.estimateCenterAndRadius(irisState.getPupilMask());
                    if (pupilEst == null) return;

                    int cx = (int) Math.round(pupilEst[0]);
                    int cy = (int) Math.round(pupilEst[1]);
                    double rPupil = pupilEst[2];

                    double rIris = biometria.iris.IrisRadiusEstimator.estimateIrisRadius(irisState.getIrisMask(), cx, cy);

                    ImageMatrix unwrapped = biometria.iris.IrisUnwrapper.unwrap(
                            editorService.getCurrent(), cx, cy, rPupil, rIris
                    );

                    unwrappedIrisPanel.setImage(unwrapped);
                },
                // 6) kod binarny
                () -> {
                    if (irisState.getPupilMask() == null || irisState.getIrisMask() == null) {
                        JOptionPane.showMessageDialog(this, "Musisz wyliczyć maski źrenicy (3a) i tęczówki (3b)!");
                        return;
                    }

                    double[] pupilEst = PupilCenterEstimator.estimateCenterAndRadius(irisState.getPupilMask());
                    if (pupilEst == null) return;

                    int cx = (int) Math.round(pupilEst[0]);
                    int cy = (int) Math.round(pupilEst[1]);
                    double rPupil = pupilEst[2];

                    double rIris = biometria.iris.IrisRadiusEstimator.estimateIrisRadius(irisState.getIrisMask(), cx, cy);

                    ImageMatrix unwrapped = biometria.iris.IrisUnwrapper.unwrap(
                            editorService.getCurrent(), cx, cy, rPupil, rIris
                    );
                    if (unwrapped == null) return;

                    double[][] bands = biometria.iris.IrisBandsFromUnwrapped.extract(
                            unwrapped,
                            biometria.iris.IrisBandsFromUnwrapped.DEFAULT_BANDS,
                            biometria.iris.IrisBandsFromUnwrapped.DEFAULT_POINTS
                    );

                    boolean[] code = biometria.iris.IrisCode.encode(
                            bands,
                            1.0 / 16.0,
                            12
                    );

                    int ones = 0;
                    for (boolean bit : code) if (bit) ones++;
                    double ratio = (double) ones / code.length;
                    System.out.println("onesRatio=" + ratio + " len=" + code.length);

                    ImageMatrix codeImg = biometria.iris.IrisCodeRenderer.render(
                            code,
                            biometria.iris.IrisBandsFromUnwrapped.DEFAULT_BANDS,
                            biometria.iris.IrisBandsFromUnwrapped.DEFAULT_POINTS,
                            4
                    );

                    unwrappedIrisPanel.setImage(codeImg);

                    // porównanie z poprzednim kodem
                    if (lastCode != null) {
                        double dNoShift = biometria.iris.Hamming.distance(lastCode, code);

                        JOptionPane.showMessageDialog(this,
                                "Porównanie z poprzednim kodem:\n" +
                                        "Odległośc Hamminga = " + String.format("%.4f", dNoShift) + "\n" +
                                        (lastFileName != null ? "poprzedni plik: " + lastFileName + "\n" : "")
                        );
                    }

                    // zapamiętanie bieżącego kodu jako poprzedni
                    lastCode = code;
                    lastFileName = fileHandler.getLastOpenedFileName();
                }
        ), BorderLayout.NORTH);

        JPanel rightMid = new JPanel();
        rightMid.setLayout(new BoxLayout(rightMid, BoxLayout.Y_AXIS));
        rightMid.setBackground(LIGHT_GRAY);
        rightMid.add(overlayControls);
        rightMid.add(Box.createVerticalStrut(10));

        rightTop.add(rightMid, BorderLayout.CENTER);

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
    }

    private void initMenu() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(MenuFactory.createFileMenu(this));
        menuBar.add(MenuFactory.createEditMenu(this));
        setJMenuBar(menuBar);
    }

    private void clearSegmentationAndOverlays() {
        irisState.setBaseGray(null);
        irisState.setPupilMask(null);
        irisState.setIrisMask(null);
        imagePanel.setEyeOverlay(false, 0, 0, 0, 0);
        if (showPupilCircleCheckBox != null) showPupilCircleCheckBox.setSelected(false);
        if (showIrisCircleCheckBox != null) showIrisCircleCheckBox.setSelected(false);
        unwrappedIrisPanel.setImage(null);
    }

    private void updatePupilOverlay() {
        if (showPupilCircleCheckBox == null) return;

        boolean enabled = showPupilCircleCheckBox.isSelected();
        if (!enabled || !validateImageLoaded()) {
            updateCombinedEyeOverlay();
            return;
        }

        if (irisState.getPupilMask() == null) {
            JOptionPane.showMessageDialog(this, "Najpierw policz maskę źrenicy (2a) / morfologię (3a).");
            showPupilCircleCheckBox.setSelected(false);
            updateCombinedEyeOverlay();
            return;
        }

        updateCombinedEyeOverlay();
    }

    private void updateIrisOverlay() {
        if (showIrisCircleCheckBox == null) return;

        boolean enabled = showIrisCircleCheckBox.isSelected();
        if (!enabled || !validateImageLoaded()) {
            updateCombinedEyeOverlay();
            return;
        }

        if (irisState.getIrisMask() == null) {
            JOptionPane.showMessageDialog(this, "Najpierw policz maskę tęczówki (2b) / morfologię (3b).");
            showIrisCircleCheckBox.setSelected(false);
            updateCombinedEyeOverlay();
            return;
        }

        updateCombinedEyeOverlay();
    }

    private void updateCombinedEyeOverlay() {
        if (!validateImageLoaded()) {
            imagePanel.setEyeOverlay(false, 0, 0, 0, 0);
            return;
        }

        boolean showPupil = showPupilCircleCheckBox != null && showPupilCircleCheckBox.isSelected();
        boolean showIris = showIrisCircleCheckBox != null && showIrisCircleCheckBox.isSelected();

        if (!showPupil && !showIris) {
            imagePanel.setEyeOverlay(false, 0, 0, 0, 0);
            return;
        }

        if (irisState.getPupilMask() == null) {
            if (showIrisCircleCheckBox != null) showIrisCircleCheckBox.setSelected(false);
            JOptionPane.showMessageDialog(this, "Najpierw policz źrenicę (maska źrenicy), żeby znać środek.");
            imagePanel.setEyeOverlay(false, 0, 0, 0, 0);
            return;
        }

        double[] pupilEst = PupilCenterEstimator.estimateCenterAndRadius(irisState.getPupilMask());
        if (pupilEst == null) {
            imagePanel.setEyeOverlay(false, 0, 0, 0, 0);
            return;
        }

        int cx = (int) Math.round(pupilEst[0]);
        int cy = (int) Math.round(pupilEst[1]);

        double rPupil = showPupil ? pupilEst[2] : 0.0;

        double rIris = 0.0;
        if (showIris) {
            if (irisState.getIrisMask() == null) {
                rIris = 0.0;
            } else {
                rIris = biometria.iris.IrisRadiusEstimator.estimateIrisRadius(irisState.getIrisMask(), cx, cy);
            }
        }
        imagePanel.setImage(editorService.getCurrent().copy());
        imagePanel.setEyeOverlay(true, cx, cy, rPupil, rIris);
    }

    void openFile() {
        if (fileHandler.openFile()) {
            clearSegmentationAndOverlays();
            refreshView();

            String fileName = fileHandler.getLastOpenedFileName();
            if (fileName != null) setTitle("Biometria - " + fileName);
        }
    }

    void saveFile() {
        fileHandler.saveFile();
    }

    void undo() {
        editorService.undo();
        clearSegmentationAndOverlays();
        refreshView();
    }

    void redo() {
        editorService.redo();
        clearSegmentationAndOverlays();
        refreshView();
    }

    void reset() {
        editorService.resetToOriginal();
        clearSegmentationAndOverlays();
        refreshView();
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
        imagePanel.setEyeOverlay(false, 0, 0, 0, 0);
    }

}