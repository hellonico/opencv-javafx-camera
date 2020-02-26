package origami;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import static org.opencv.core.Core.addWeighted;

/**
 * https://amehta.github.io/posts/2019/09/create-and-apply-simple-filters-to-an-image-using-opencv-and-python/
 */
public class SunGlasses implements Filter {
    public SunGlasses(Scalar scalar) {
        this.color = scalar;
    }

    public static class Red extends SunGlasses {
        public Red() {
            super(new Scalar(0, 0, 255));
        }
    }

    public static class Blue extends SunGlasses {
        public Blue() {
            super(new Scalar(255, 0, 0));
        }
    }

    public static class Turquoise extends SunGlasses {
        public Turquoise() {
            super(new Scalar(208, 224, 64));
        }
    }

    public static class Green extends SunGlasses {
        public Green() {
            super(new Scalar(0, 255, 0));
        }
    }

    public static class Purple extends SunGlasses {
        public Purple() {
            super(new Scalar(128, 0, 128));
        }
    }

    public static class Yellow extends SunGlasses {
        public Yellow() {
            super(new Scalar(0, 255, 255));
        }
    }

    public static class Salmon extends SunGlasses {
        public Salmon() {
            super(new Scalar(114, 128, 250));
        }
    }

    double alpha = 0.8;
    double beta = 0.2;
    double gamma = 0;
    Scalar color = new Scalar(0, 0, 255);


    public Mat apply(Mat mat) {
        Mat result = new Mat();
        Mat glasses = new Mat(mat.size(), mat.type(), color);
        addWeighted(mat, alpha, glasses, beta, gamma, result);
        return result;
    }
}
