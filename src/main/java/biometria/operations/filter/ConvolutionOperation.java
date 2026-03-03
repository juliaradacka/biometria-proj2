package biometria.operations.filter;

import biometria.model.ImageMatrix;
import biometria.operations.ImageOperation;
import biometria.util.ColorUtil;

public class ConvolutionOperation implements ImageOperation {

    private final double[][] mask;
    private final double weight;


    public ConvolutionOperation(double[][] mask, double weight) {
        this.mask = mask;
        this.weight = (weight == 0) ? 1 : weight;
    }

    @Override
    public ImageMatrix apply(ImageMatrix input) {
        int w = input.getWidth();
        int h = input.getHeight();
        ImageMatrix output = new ImageMatrix(w,h);

        int offset = mask.length / 2;

        for(int y=0;y<h;y++){
            for(int x=0;x<w;x++){

                if(x < offset || x >= w - offset || y < offset || y >= h - offset){
                    output.setARGB(x,y,input.getARGB(x,y));
                    continue;
                }

                double sumR = 0, sumG = 0, sumB = 0;
                int a = ColorUtil.getAlpha(input.getARGB(x,y));
                // nakladamy maske 3x3 na sąsiadów
                for (int my = 0; my < mask.length; my++){
                    for (int mx = 0; mx < mask[0].length; mx++){
                        int pixelX = x + mx - offset;
                        int pixelY = y + my - offset;
                        int argb = input.getARGB(pixelX,pixelY);
                        double maskValue = mask[my][mx];

                        sumR += ColorUtil.getRed(argb) * maskValue;
                        sumB += ColorUtil.getBlue(argb) * maskValue;
                        sumG += ColorUtil.getGreen(argb) * maskValue;


                    }
                }

                int newR = ColorUtil.clamp((int) (sumR / weight));
                int newG = ColorUtil.clamp((int) (sumG / weight));
                int newB = ColorUtil.clamp((int) (sumB / weight));

                output.setARGB(x,y,ColorUtil.toARGB(a,newR, newG, newB));
            }
        }

        return output;
    }
}
