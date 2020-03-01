package origami;

import org.opencv.core.Mat;

import static org.opencv.core.Core.bitwise_not;
import static org.opencv.imgproc.Imgproc.Canny;
import static org.opencv.imgproc.Imgproc.cvtColor;

public class CoolCanny implements Filter {

    public Mat apply(Mat in) {
        Mat dst = new Mat();
        Canny(in, dst, 500, 500, 5, true);
//        bitwise_not(dst, dst, new Mat());
        cvtColor(dst, dst, 8);

        return dst;
    }
}
