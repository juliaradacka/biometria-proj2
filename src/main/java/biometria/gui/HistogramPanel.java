package biometria.gui;

import biometria.model.ImageMatrix;
import biometria.util.ColorUtil;

import javax.swing.*;
import java.awt.*;

public class HistogramPanel extends JPanel {

    private int[] counts;
    private int maxCount;


    public HistogramPanel(ImageMatrix image){
        calculateHistogram(image);
        setPreferredSize(new Dimension(512,200));

    }

    public void updateHistogram(ImageMatrix image) {
        calculateHistogram(image);
        repaint();
    }

    private void calculateHistogram(ImageMatrix image){
        counts = new int[256];
        maxCount = 0;

        int w = image.getWidth();
        int h = image.getHeight();

        for(int x=0;x<w;x++){
            for(int y=0;y<h;y++){
                int argb = image.getARGB(x,y);
                int r = ColorUtil.getRed(argb);
                int b = ColorUtil.getBlue(argb);
                int g = ColorUtil.getGreen(argb);

                int luminance = (int) (0.299 * r + 0.587 * g + 0.114 * b);

                counts[luminance]++;
                if(maxCount < counts[luminance]){
                    maxCount = counts[luminance];
                }

            }
        }
    }

    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        if(counts == null || maxCount == 0){
            return;
        }
        int width = getWidth();
        int height = getHeight();
        int padding = 25;

        int graphWidth = width - (2 * padding);
        int graphHeight = height - (2 * padding);
        g.setColor(Color.WHITE);
        g.fillRect(0,0,width,height);
        g.setColor(Color.DARK_GRAY);
        double barWidth = (double) graphWidth / 256;

        for(int i=0; i < 256; i++){
            int barHeight = (int) (((double) counts[i] / maxCount) * graphHeight);

            int x = padding + (int) ( i * barWidth);
            int nextX = padding + (int) ( (i + 1) * barWidth);
            int currentBarWidth = Math.max(nextX - x,1);
            int y = height - barHeight - padding;

            g.fillRect(x, y, currentBarWidth, barHeight);
        }

        g.setColor(Color.BLACK);
        g.drawLine(padding, height - padding, width - padding, height - padding);
        g.drawLine(padding, height - padding, padding, padding);

        g.drawString("0", padding, height - padding + 15);
        g.drawString("128", width / 2 - 10, height - padding + 15);
        g.drawString("255", width - padding - 20, height - padding + 15);


    }


}
