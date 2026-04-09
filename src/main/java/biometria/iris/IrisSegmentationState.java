package biometria.iris;

import biometria.model.ImageMatrix;

public class IrisSegmentationState {
    private ImageMatrix baseGray;
    private ImageMatrix pupilMask;
    private ImageMatrix irisMask;

    public ImageMatrix getBaseGray() { return baseGray; }
    public void setBaseGray(ImageMatrix baseGray) { this.baseGray = baseGray; }

    public ImageMatrix getPupilMask() { return pupilMask; }
    public void setPupilMask(ImageMatrix pupilMask) { this.pupilMask = pupilMask; }

    public ImageMatrix getIrisMask() { return irisMask; }
    public void setIrisMask(ImageMatrix irisMask) { this.irisMask = irisMask; }
}