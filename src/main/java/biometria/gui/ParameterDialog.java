package biometria.gui;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

import biometria.operations.morphology.StructuringElementShape;

public class ParameterDialog {

    public static Integer showSliderDialog(
            Component parent,
            String title,
            String label,
            int min,
            int max,
            int initial,
            Consumer<Integer> previewCallback
    ) {

        JPanel panel = createSliderPanel(label, min, max, initial, previewCallback);

        int result = JOptionPane.showConfirmDialog(
                parent,
                panel,
                title,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            return (Integer) panel.getClientProperty("value");
        }

        return null;
    }


    private static JPanel createSliderPanel(String label, int min, int max, int initial, Consumer<Integer> previewCallback) {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel(label);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(15));

        JSlider slider = new JSlider(JSlider.HORIZONTAL, min, max, initial);
        slider.setMajorTickSpacing(Math.max(1, (max - min) / 5));
        slider.setMinorTickSpacing(Math.max(1, (max - min) / 20));
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(slider);
        mainPanel.add(Box.createVerticalStrut(15));

        JPanel valuePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        valuePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel valueLabel = new JLabel("Dokładna wartość:");
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(initial, min, max, 1));
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setColumns(5);

        valuePanel.add(valueLabel);
        valuePanel.add(spinner);
        mainPanel.add(valuePanel);


        slider.addChangeListener(e -> {
            int val = slider.getValue();
            spinner.setValue(val);
            mainPanel.putClientProperty("value", val);

            if (previewCallback != null) {
                previewCallback.accept(val);
            }
        });


        spinner.addChangeListener(e -> {
            int val = (Integer) spinner.getValue();
            slider.setValue(val);
            mainPanel.putClientProperty("value", val);

            if (previewCallback != null) {
                previewCallback.accept(val);
            }
        });

        mainPanel.putClientProperty("value", initial);

        return mainPanel;
    }
    public static class MorphParams {
        public final int size;
        public final StructuringElementShape shape;
        public final int iterations; // używaj tylko dla erozji/dylatacji

        public MorphParams(int size, StructuringElementShape shape, int iterations) {
            this.size = size;
            this.shape = shape;
            this.iterations = iterations;
        }
    }

    public static MorphParams showMorphologyDialog(
            Component parent,
            String title,
            boolean includeIterations
    ) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        SpinnerNumberModel sizeModel = new SpinnerNumberModel(3, 3, 21, 2);
        JSpinner sizeSpinner = new JSpinner(sizeModel);

        JComboBox<StructuringElementShape> shapeCombo =
                new JComboBox<>(StructuringElementShape.values());

        panel.add(new JLabel("Rozmiar SE (nieparzysty):"));
        panel.add(sizeSpinner);
        panel.add(Box.createVerticalStrut(10));

        panel.add(new JLabel("Kształt SE:"));
        panel.add(shapeCombo);

        JSpinner iterSpinner = null;
        if (includeIterations) {
            panel.add(Box.createVerticalStrut(10));
            panel.add(new JLabel("Iteracje:"));
            iterSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
            panel.add(iterSpinner);
        }

        int result = JOptionPane.showConfirmDialog(
                parent,
                panel,
                title,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) return null;

        int size = (Integer) sizeSpinner.getValue();
        StructuringElementShape shape =
                (StructuringElementShape) shapeCombo.getSelectedItem();
        if (shape == null) shape = StructuringElementShape.RECT;

        int it = includeIterations ? (Integer) iterSpinner.getValue() : 1;

        return new MorphParams(size, shape, it);
    }
}