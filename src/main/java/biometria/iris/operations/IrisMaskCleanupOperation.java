package biometria.iris.operations;

import biometria.model.ImageMatrix;
import biometria.operations.ImageOperation;
import biometria.operations.morphology.ClosingOperation;
import biometria.operations.morphology.OpeningOperation;
import biometria.operations.morphology.RepeatOperation;
import biometria.operations.morphology.StructuringElementShape;

public class IrisMaskCleanupOperation implements ImageOperation {

    private static final StructuringElementShape SHAPE = StructuringElementShape.ELLIPSE;

    private static final double ROI_SCALE_FROM_PUPIL = 3.0;
    private static final int ROI_MIN_MARGIN_PX = 20;

    private static final int OPENING1_SIZE = 9;
    private static final int OPENING1_TIMES = 1;

    private static final int CLOSING1_SIZE = 17;
    private static final int CLOSING1_TIMES = 1;

    private static final int CLOSING2_SIZE = 7;
    private static final int CLOSING2_TIMES = 1;

    private static final int OPENING2_SIZE = 5;
    private static final int OPENING2_TIMES = 1;

    private final ImageMatrix pupilMaskClean;

    public IrisMaskCleanupOperation(ImageMatrix pupilMaskClean) {
        if (pupilMaskClean == null) throw new IllegalArgumentException("pupilMaskClean == null");
        this.pupilMaskClean = pupilMaskClean;
    }

    @Override
    public ImageMatrix apply(ImageMatrix irisMask) {
        if (irisMask == null) throw new IllegalArgumentException("irisMask == null");

        int[] bb = BinaryMaskOps.blackBoundingBox(pupilMaskClean);
        if (bb == null) {
            // fallback
            ImageMatrix out = irisMask.copy();
            out = new RepeatOperation(new OpeningOperation(OPENING1_SIZE, SHAPE), OPENING1_TIMES).apply(out);
            out = new RepeatOperation(new ClosingOperation(CLOSING1_SIZE, SHAPE), CLOSING1_TIMES).apply(out);
            out = new RepeatOperation(new OpeningOperation(OPENING2_SIZE, SHAPE), OPENING2_TIMES).apply(out);
            return out;
        }

        int minX = bb[0], minY = bb[1], maxX = bb[2], maxY = bb[3];
        int bw = (maxX - minX + 1);
        int bh = (maxY - minY + 1);

        int cx = (minX + maxX) / 2;
        int cy = (minY + maxY) / 2;

        double rBox = 0.5 * Math.max(bw, bh);
        int margin = (int) Math.round(Math.max(ROI_MIN_MARGIN_PX, ROI_SCALE_FROM_PUPIL * rBox));

        int x0 = cx - margin, x1 = cx + margin;
        int y0 = cy - margin, y1 = cy + margin;

        ImageMatrix roiIris = BinaryMaskOps.keepOnlyRoi(irisMask.copy(), x0, y0, x1, y1);

        // 1) Na czas czyszczenia usuń pupil z iris (żeby closing nie sklejał syfu do środka)
        ImageMatrix ringOnly = BinaryMaskOps.subtractBlack(roiIris, pupilMaskClean);

        // 2) Morfologia na ringOnly
        ImageMatrix cleanedRing = new RepeatOperation(new OpeningOperation(OPENING1_SIZE, SHAPE), OPENING1_TIMES)
                .apply(ringOnly);

        cleanedRing = new RepeatOperation(new ClosingOperation(CLOSING1_SIZE, SHAPE), CLOSING1_TIMES)
                .apply(cleanedRing);

        cleanedRing = new RepeatOperation(new ClosingOperation(CLOSING2_SIZE, SHAPE), CLOSING2_TIMES)
                .apply(cleanedRing);

        cleanedRing = new RepeatOperation(new OpeningOperation(OPENING2_SIZE, SHAPE), OPENING2_TIMES)
                .apply(cleanedRing);

        // 3) Przywróć pupil jako czarny (finalna maska ma pupil+tęczówkę na czarno)
        return BinaryMaskOps.orBlack(cleanedRing, pupilMaskClean);
    }
}