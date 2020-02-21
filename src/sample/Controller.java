package sample;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import origami.Filter;
import origami.filters.Cartoon2;

import java.io.ByteArrayInputStream;

public class Controller {

    @FXML
    private ImageView mat;
    boolean start=false;

    public void click() {
        if(!start) {
            startCamera();
        } else {
//            mat.imageProperty().set(null);
        }
        start=!start;
    }

    private Image mat2Image(Mat frame)
    {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", frame, buffer);
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }

    public void startCamera() {
        new Thread(()-> {
            VideoCapture cap = new VideoCapture(0);
            Mat var1 = new Mat();
            Filter f = new Cartoon2();
            while(start && cap.grab()) {
                cap.retrieve(var1);
                mat.setImage(mat2Image(f.apply(var1)));
            }
            cap.release();
        }).start();
    }
}
