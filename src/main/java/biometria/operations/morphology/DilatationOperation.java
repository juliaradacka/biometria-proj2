package biometria.operations.morphology;

import biometria.model.ImageMatrix;
import biometria.operations.ImageOperation;
import biometria.operations.point.BinarizationOperation;
import biometria.util.ColorUtil;

public class DilatationOperation implements ImageOperation {
    @Override
    public ImageMatrix apply(ImageMatrix input) {


        int width = input.getWidth();
        int height = input.getHeight();

        BinarizationOperation binarizationOperation = new BinarizationOperation(128);
        ImageMatrix binaryImage = binarizationOperation.apply(input);

        ImageMatrix result = binaryImage.copy();

        for(int y=1; y<height-1; y++){
            for(int x=1; x<width-1; x++){
                int maxVal = 0;

                for(int ky=-1; ky<=1; ky++){
                    for(int kx=-1; kx<=1; kx++){
                        int argb = binaryImage.getARGB(x+kx, y+ky);
                        int gray = ColorUtil.getRed(argb);
                        maxVal = Math.max(maxVal, gray);
                    }
                }

                result.setARGB(x,y,ColorUtil.toARGB(255, maxVal,maxVal,maxVal));
            }
        }


        return result;
    }
}
