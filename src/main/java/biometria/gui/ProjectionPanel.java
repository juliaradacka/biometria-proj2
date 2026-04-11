package biometria.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;

public class ProjectionPanel extends JPanel {

    private int[] values;
    private int maxValue;

    private final String title;
    private final boolean topMode;

    public ProjectionPanel(String title, boolean topMode) {
        this.title = title;
        this.topMode = topMode;
        setPreferredSize(new Dimension(350,200));
        setBackground(Color.WHITE);
        setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    public void updateProjection(int[] values) {
        this.values = values;
        this.maxValue = 0;

        if (values != null) {
            for (int v : values) {
                if (v > maxValue) maxValue = v;
            }
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int width = getWidth();
        int height = getHeight();

        int left = 26;
        int right = 14;
        int top = 22;
        int bottom = 18;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, width, height);

        g2.setColor(Color.BLACK);
        g2.drawString(title, left, 14);

        int x = left;
        int y = top;
        int w = width - left - right;
        int h = height - top - bottom;

        if (topMode) {
            drawAxesTop(g2, x, y, w, h);
        } else {
            drawAxesRight(g2, x, y, w, h);
        }

        if (values != null && values.length > 0 && maxValue > 0) {
            g2.setColor(new Color(70, 70, 70));
            if (topMode) {
                drawAreaTop(g2, x, y, w, h);
            } else {
                drawAreaRight(g2, x, y, w, h);
            }
        }

        g2.dispose();
    }

    private void drawAxesTop(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(Color.GRAY);

        int x0 = x;
        int y0 = y + h;

        g2.drawLine(x0, y, x0, y0);
        g2.drawLine(x0, y0, x0 + w, y0);

        g2.fillPolygon(triangleUp(x0, y));
        g2.fillPolygon(triangleRight(x0 + w, y0));

        g2.drawString("0", x0 - 10, y0 + 12);
        g2.drawString("", x0 + w - 60, y0 + 12);
    }

    private void drawAxesRight(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(Color.GRAY);

        int x0 = x;
        int y0 = y;

        g2.drawLine(x0, y0, x0, y0 + h);
        g2.drawLine(x0, y0, x0 + w, y0);

        g2.fillPolygon(triangleDown(x0, y0 + h));
        g2.fillPolygon(triangleRight(x0 + w, y0));

        g2.drawString("0", x0 - 10, y0 + 10);
        g2.drawString("", x0 - 20, y0 + h);
    }

    private void drawAreaTop(Graphics2D g2, int x, int y, int w, int h) {
        Path2D path = new Path2D.Double();

        double dx = (values.length == 1) ? 0 : (double) w / (values.length - 1);
        int baseY = y + h;

        path.moveTo(x, baseY);

        for (int i = 0; i < values.length; i++) {
            double xx = x + i * dx;
            double frac = (double) values[i] / maxValue;
            double yy = baseY - frac * h;
            path.lineTo(xx, yy);
        }

        path.lineTo(x + w, baseY);
        path.closePath();

        g2.fill(path);
    }

    private void drawAreaRight(Graphics2D g2, int x, int y, int w, int h) {
        Path2D path = new Path2D.Double();

        double dy = (values.length == 1) ? 0 : (double) h / (values.length - 1);
        int baseX = x;

        path.moveTo(baseX, y);

        for (int i = 0; i < values.length; i++) {
            double yy = y + i * dy;
            double frac = (double) values[i] / maxValue;
            double xx = baseX + frac * w;
            path.lineTo(xx, yy);
        }

        path.lineTo(baseX, y + h);
        path.closePath();

        g2.fill(path);
    }

    private Polygon triangleUp(int x, int y) {
        int s = 6;
        Polygon p = new Polygon();
        p.addPoint(x, y);
        p.addPoint(x - s, y + s);
        p.addPoint(x + s, y + s);
        return p;
    }

    private Polygon triangleDown(int x, int y) {
        int s = 6;
        Polygon p = new Polygon();
        p.addPoint(x, y);
        p.addPoint(x - s, y - s);
        p.addPoint(x + s, y - s);
        return p;
    }

    private Polygon triangleRight(int x, int y) {
        int s = 6;
        Polygon p = new Polygon();
        p.addPoint(x, y);
        p.addPoint(x - s, y - s);
        p.addPoint(x - s, y + s);
        return p;
    }
}