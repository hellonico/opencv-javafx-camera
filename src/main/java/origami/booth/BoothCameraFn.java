package origami.booth;

import javafx.stage.Stage;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import origami.Camera;
import origami.CameraFn;
import origami.Origami;
import origami.OrigamiFX;

public class BoothCameraFn implements CameraFn {

    private final Controller c;

    public BoothCameraFn(Controller c) {
        this.c = c;
    }

    @Override
    public Mat read(Camera camera, Mat mat) {
        c.last = mat;

        c.mat.setImage(OrigamiFX.toFXImage(mat));

        // TODO: move somewhere else
        // https://stackoverflow.com/questions/12630296/resizing-images-to-fit-the-parent-node
        double width = Stage.getWindows().get(0).getWidth();
        double height = Stage.getWindows().get(0).getHeight();
        double ratio = (double) mat.width() / mat.height();
        c.mat.setFitWidth(width);
        c.mat.setFitHeight(height);
//        c.mat.setFitHeight(width/ratio);
//        c.mat.setPreserveRatio(false);

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
