package origami.booth;

import org.opencv.core.Mat;
import origami.Camera;
import origami.CameraFn;
import origami.OrigamiFX;

public class BoothCameraFn implements CameraFn {

    private final Controller c;

    public BoothCameraFn(Controller c) {
        this.c = c;
    }

    @Override
    public Mat read(Camera camera, Mat mat) {
        c.last = mat;

        c.mat.setImage(OrigamiFX.toFXImage(c.last));

        if (c.detectorObject != null) {
            if (c.detectorObject.detected(c.last)) {
                c.preview.setImage(OrigamiFX.toFXImage(c.detectorObject.detectMats(c.last.clone()).get(0)));
            } else {
                c.preview.setImage(null);
            }
        }
        return mat;
    }
}
