package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import origami.Filter;
import origami.Filters;
import origami.filters.FPS;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    private ImageView mat;
    boolean start = false;

    @FXML
    private ComboBox<String> filters;

    @FXML
    CheckBox fps;

    @FXML
    TextField vid;

    @FXML
    TextField message;

    ObservableList<String> options =
            FXCollections.observableArrayList();
    private Mat last = new Mat();

    private Image mat2Image(Mat frame) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", frame, buffer);
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }

    Filter f;

    private void message(String e) {
        System.out.println(e);
        message.setText(e);
    }

    Mat buffer = new Mat();

    public void startCamera() {
        new Thread(() -> {
            VideoCapture cap = null;
            String _vid = vid.getText();
            try {
                int device = Integer.parseInt(_vid);
                cap = new VideoCapture(device);
                message("Open Device " + device);
            } catch (Exception e) {
                message(e.getMessage());
                File f = new File(_vid);
                if (f.isFile()) {
                    cap = new VideoCapture(f.getAbsolutePath());
                    message("Open File:" + f.getName());
                } else {
                    try {
                        cap = new VideoCapture(_vid);
                        message("Open URL:" + _vid);
                    } catch (Exception e_) {
//                        e_.printStackTrace();
                        message("Can't open [" + _vid + "] ...");
                        cap = new VideoCapture(0);
                        message("Open Device 0");
                    }
                }
            }

            while (start && cap.grab()) {
                cap.retrieve(buffer);
                last = f.apply(buffer);
                mat.setImage(mat2Image(last));
            }
            message("Stream ended...");
            cap.release();
        }).start();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        options.addAll(FindFilters.findFilters());
        filters.setItems(options);
        f = mat -> mat;
        filters.getSelectionModel().selectedItemProperty().addListener(
                (Observable, oldValue, newValue) -> {
                    updateFilter();
                });
    }

    public void stream(ActionEvent actionEvent) {
        if (!start) {
            startCamera();
        } else {
            mat.setImage(mat2Image(new Mat(1, 1, CvType.CV_8UC3)));
        }
        start = !start;
    }

    public void check(ActionEvent actionEvent) {
        updateFilter();
    }

    private void updateFilter() {

        if (fps.isSelected()) {
            f = new Filters(getCurrentFilter(), new FPS());
        } else {
            f = getCurrentFilter();
        }
    }

    private Filter getCurrentFilter() {
        String current = filters.getValue();
        try {
            Class klass = Class.forName(current);
            return (Filter) klass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            message("Can't load:" + current);
            return f -> f;
        }
    }

    public void takeShot(ActionEvent actionEvent) {
        String file = new Date().toString() + ".png";
        Imgcodecs.imwrite(file, last);
        message(file + " was saved");
    }
}
