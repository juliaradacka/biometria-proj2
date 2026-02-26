package biometria.gui;

import biometria.model.ImageMatrix;
import static biometria.gui.UIConstants.*;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ImagePanel extends JPanel {

    private BufferedImage image;

    public ImagePanel() {
        setBackground(LIGHT_GREY);
        setPreferredSize(new Dimension(800, 600));
    }

    public void setImage(ImageMatrix matrix) {
        if (matrix != null) {
            this.image = matrix.toBufferedImage(true);
        } else {
            this.image = null;
        }
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image == null) {
            return;
        }

        int panelW = getWidth();
        int panelH = getHeight();
        int imgW = image.getWidth();
        int imgH = image.getHeight();

        double scale = Math.min((double) panelW / imgW, (double) panelH / imgH);
        scale = Math.min(scale, 1.0);

        int drawW = (int) (imgW * scale);
        int drawH = (int) (imgH * scale);

        int x = (panelW - drawW) / 2;
        int y = (panelH - drawH) / 2;

        g.drawImage(image, x, y, drawW, drawH, null);
    }
}