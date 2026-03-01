package biometria.gui.dialogs;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class BrightnessDialog extends JDialog {

    private final JSlider slider;
    private final JLabel valueLabel;
    private int brightnessValue = 0;
    private boolean confirmed = false;

    public BrightnessDialog(JFrame parent) {
        super(parent, "Korekta jasności", true);

        setLayout(new BorderLayout(10, 10));

        // Panel główny
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Etykieta z aktualną wartością
        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel titleLabel = new JLabel("Jasność: ");
        valueLabel = new JLabel("0");
        valueLabel.setFont(new Font("Arial", Font.BOLD, 16));
        labelPanel.add(titleLabel);
        labelPanel.add(valueLabel);
        mainPanel.add(labelPanel);

        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Slider od -255 do +255
        slider = new JSlider(JSlider.HORIZONTAL, -255, 255, 0);
        slider.setMajorTickSpacing(50);
        slider.setMinorTickSpacing(10);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setPreferredSize(new Dimension(400, 80));

        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                brightnessValue = slider.getValue();
                valueLabel.setText(String.valueOf(brightnessValue));
            }
        });

        mainPanel.add(slider);

        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Opis
        JLabel descLabel = new JLabel("<html><center>" +
                "Wartości ujemne: obraz ciemniejszy<br>" +
                "Wartości dodatnie: obraz jaśniejszy" +
                "</center></html>");
        descLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        descLabel.setForeground(Color.GRAY);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(descLabel);

        add(mainPanel, BorderLayout.CENTER);

        // Przyciski OK/Anuluj
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton okButton = new JButton("Zastosuj");
        okButton.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        JButton cancelButton = new JButton("Anuluj");
        cancelButton.addActionListener(e -> {
            confirmed = false;
            dispose();
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(okButton);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public int getBrightnessValue() {
        return brightnessValue;
    }
}