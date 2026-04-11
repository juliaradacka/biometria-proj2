package biometria.gui;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

import static biometria.gui.UIConstants.LIGHT_GRAY;

public class IrisSegmentationPanel extends JPanel {

    private final Runnable onApplyGrayBase;
    private final Consumer<Double> onComputePupilMask;
    private final Consumer<Double> onComputeIrisMask;

    private final Runnable onMorphPupil;

    public IrisSegmentationPanel(
            Runnable onApplyGrayBase,
            Consumer<Double> onComputePupilMask,
            Consumer<Double> onComputeIrisMask,
            Runnable onMorphPupil
    ) {
        this.onApplyGrayBase = onApplyGrayBase;
        this.onComputePupilMask = onComputePupilMask;
        this.onComputeIrisMask = onComputeIrisMask;
        this.onMorphPupil = onMorphPupil;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder("Proces segmentacji oka"));
        setBackground(LIGHT_GRAY);

        initUi();
    }

    private void initUi() {
        JButton btnGray = new JButton("1. Przekształć do szarości (baza)");
        btnGray.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnGray.addActionListener(e -> onApplyGrayBase.run());

        // progi
        JPanel pnlPupil = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlPupil.setBackground(LIGHT_GRAY);
        pnlPupil.add(new JLabel("X_P (Źrenica):"));

        JPanel pnlIris = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlIris.setBackground(LIGHT_GRAY);
        pnlIris.add(new JLabel("X_I (Tęczówka):"));

        SpinnerModel xpModel = new SpinnerNumberModel(2.0, 0.1, 10.0, 0.1);
        JSpinner spinnerXp = new JSpinner(xpModel);
        pnlPupil.add(spinnerXp);

        SpinnerModel xiModel = new SpinnerNumberModel(2.0, 0.1, 10.0, 0.1);
        JSpinner spinnerXi = new JSpinner(xiModel);
        pnlIris.add(spinnerXi);

        JButton btnBinPupil = new JButton("2a. Policz maskę Źrenicy (podgląd)");
        btnBinPupil.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnBinPupil.addActionListener(e -> onComputePupilMask.accept((Double) spinnerXp.getValue()));

        JButton btnBinIris = new JButton("2b. Policz maskę Tęczówki (podgląd)");
        btnBinIris.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnBinIris.addActionListener(e -> onComputeIrisMask.accept((Double) spinnerXi.getValue()));

        JPanel pnlMorph = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlMorph.setBackground(LIGHT_GRAY);

        JButton btnMorphPupil = new JButton("3a. Morfologia Źrenicy");
        btnMorphPupil.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnMorphPupil.addActionListener(e -> onMorphPupil.run());
        pnlMorph.add(btnMorphPupil);

        add(Box.createVerticalStrut(5));
        add(btnGray);
        add(Box.createVerticalStrut(5));
        add(pnlPupil);
        add(pnlIris);
        add(btnBinPupil);
        add(btnBinIris);
        add(Box.createVerticalStrut(8));
        add(pnlMorph);
        add(Box.createVerticalStrut(5));
    }
}