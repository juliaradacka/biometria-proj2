package biometria.gui;

import javax.swing.*;
import java.awt.*;

public class CustomConvolutionDialog extends JDialog {

    public static class Kernel {
        public final double[][] mask;
        public final double weight;

        public Kernel(double[][] mask, double weight) {
            this.mask = mask;
            this.weight = weight;
        }
    }

    private Kernel result;

    private final JComboBox<Integer> sizeCombo;
    private final JPanel gridPanel;
    private JTextField[][] fields;

    private final JCheckBox autoWeightCheck;
    private final JTextField weightField;

    private CustomConvolutionDialog(Frame owner) {
        super(owner, "Filtr własny (splot)", true);

        sizeCombo = new JComboBox<>(new Integer[]{3, 5, 7});

        autoWeightCheck = new JCheckBox("Auto waga (suma maski)", true);
        weightField = new JTextField("1.0", 8);

        gridPanel = new JPanel();

        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Anuluj");

        ok.addActionListener(e -> onOk());
        cancel.addActionListener(e -> {
            result = null;
            dispose();
        });

        sizeCombo.addActionListener(e -> rebuildGrid((Integer) sizeCombo.getSelectedItem()));

        autoWeightCheck.addActionListener(e -> weightField.setEnabled(!autoWeightCheck.isSelected()));
        weightField.setEnabled(!autoWeightCheck.isSelected());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        top.add(new JLabel("Rozmiar:"));
        top.add(sizeCombo);

        JPanel weightPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        weightPanel.add(autoWeightCheck);
        weightPanel.add(new JLabel("Waga (dzielnik):"));
        weightPanel.add(weightField);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        buttons.add(cancel);
        buttons.add(ok);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        top.setAlignmentX(Component.LEFT_ALIGNMENT);
        gridPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        weightPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(top);
        content.add(Box.createVerticalStrut(6));
        content.add(gridPanel);
        content.add(Box.createVerticalStrut(6));
        content.add(weightPanel);
        content.add(Box.createVerticalStrut(8));
        content.add(buttons);

        setContentPane(content);

        rebuildGrid(3);

        pack();
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    public static Kernel showDialog(Component parent) {
        Frame owner = JOptionPane.getFrameForComponent(parent);
        CustomConvolutionDialog dialog = new CustomConvolutionDialog(owner);
        dialog.setVisible(true);
        return dialog.result;
    }

    private void rebuildGrid(int size) {
        gridPanel.removeAll();
        gridPanel.setLayout(new GridLayout(size, size, 4, 4));

        fields = new JTextField[size][size];

        int cy = size / 2;
        int cx = size / 2;

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                JTextField tf = new JTextField(4);
                tf.setHorizontalAlignment(SwingConstants.CENTER);

                // domyślnie maska jednostkowa: środek=1, reszta=0
                tf.setText((x == cx && y == cy) ? "1" : "0");

                fields[y][x] = tf;
                gridPanel.add(tf);
            }
        }

        gridPanel.revalidate();
        gridPanel.repaint();
        pack();
    }

    private void onOk() {
        try {
            double[][] mask = readMask();
            double weight;

            if (autoWeightCheck.isSelected()) {
                weight = sum(mask);
                if (isAlmostZero(weight)) weight = 1.0;
            } else {
                weight = parseDouble(weightField.getText());
                if (isAlmostZero(weight)) weight = 1.0;
            }

            result = new Kernel(mask, weight);
            dispose();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Błędne dane",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private double[][] readMask() {
        if (fields == null || fields.length == 0) {
            throw new IllegalArgumentException("Brak siatki maski.");
        }

        int n = fields.length;
        double[][] mask = new double[n][n];

        for (int y = 0; y < n; y++) {
            for (int x = 0; x < n; x++) {
                mask[y][x] = parseDouble(fields[y][x].getText());
            }
        }
        return mask;
    }

    private static double parseDouble(String s) {
        if (s == null) throw new IllegalArgumentException("Puste pole.");
        String t = s.trim();
        if (t.isEmpty()) throw new IllegalArgumentException("Puste pole.");

        // wsparcie dla przecinka dziesiętnego
        t = t.replace(',', '.');

        try {
            return Double.parseDouble(t);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Nieprawidłowa liczba: \"" + s + "\"");
        }
    }

    private static double sum(double[][] m) {
        double s = 0.0;
        for (double[] row : m) {
            for (double v : row) s += v;
        }
        return s;
    }

    private static boolean isAlmostZero(double v) {
        return Math.abs(v) < 1e-12;
    }
}