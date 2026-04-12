package biometria.gui;

import biometria.model.ImageMatrix;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import static biometria.gui.UIConstants.*;

public class ImagePanel extends JPanel {

    private BufferedImage image;

    private boolean showCircle = false;
    private int circleCx = 0;
    private int circleCy = 0;
    private double circleR = 0;

    public ImagePanel() {
        setBackground(LIGHT_GRAY);
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

    public void setCircleOverlay(boolean enabled, int cx, int cy, double r) {
        this.showCircle = enabled;
        this.circleCx = cx;
        this.circleCy = cy;
        this.circleR = r;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image == null) return;

        int panelW = getWidth();
        int panelH = getHeight();
        int imgW = image.getWidth();
        int imgH = image.getHeight();

        double scale = Math.min((double) panelW / imgW, (double) panelH / imgH);
        scale = Math.min(scale, 1.0);

        int drawW = (int) Math.round(imgW * scale);
        int drawH = (int) Math.round(imgH * scale);

        int x0 = (panelW - drawW) / 2;
        int y0 = (panelH - drawH) / 2;

        g.drawImage(image, x0, y0, drawW, drawH, null);

        if (showCircle && circleR > 0) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setStroke(new BasicStroke(2.0f));
                g2.setColor(Color.RED);

                int cx = x0 + (int) Math.round(circleCx * scale);
                int cy = y0 + (int) Math.round(circleCy * scale);
                int rr = (int) Math.round(circleR * scale);

                g2.drawOval(cx - rr, cy - rr, 2 * rr, 2 * rr);
                g2.fillOval(cx - 2, cy - 2, 4, 4);
            } finally {
                g2.dispose();
            }
        }
    }
}