package sample;

import static org.opencv.videoio.Videoio.CAP_PROP_FRAME_HEIGHT;
import static org.opencv.videoio.Videoio.CAP_PROP_FRAME_WIDTH;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

import javafx.scene.input.KeyEvent;
import org.apache.commons.lang3.StringUtils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

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
import origami.Camera;
import origami.Filter;
import origami.Filters;
import origami.StoppableCamera;
import origami.filters.FPS;

public class Controller implements Initializable {

    @FXML
    private ImageView mat;
    boolean start = false;

    @FXML
    private ComboBox<String> filters;

    @FXML
    CheckBox fps;
    //
    // @FXML
    // Button stream;

    @FXML
    TextField vid;

    @FXML
    TextField message;

    @FXML
    TextField width;

    @FXML
    TextField height;

    ObservableList<String> options = FXCollections.observableArrayList();
    private Mat last = new Mat();

    @FXML
    CheckBox fullscreen;

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
    StoppableCamera fullscreenCam;

    public void startCamera() {
        new Thread(() -> {

            VideoCapture cap = null;
            String _vid = vid.getText();
            try {
                if (_vid.strip().equalsIgnoreCase(""))
                    _vid = "0";
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
                        // e_.printStackTrace();
                        message("Can't open [" + _vid + "] ...");
                        cap = new VideoCapture(0);
                        message("Open Device 0");
                    }
                }
            }

            if (!width.getText().equalsIgnoreCase("") && !height.getText().equalsIgnoreCase("")) {
                cap.set(CAP_PROP_FRAME_WIDTH, Integer.parseInt(width.getText()));
                cap.set(CAP_PROP_FRAME_HEIGHT, Integer.parseInt(height.getText()));
            }
            message(">> stream: " + cap.get(CAP_PROP_FRAME_WIDTH) + "x" + cap.get(CAP_PROP_FRAME_HEIGHT));

            if (fullscreen.isSelected()) {
                fullscreenCam = new StoppableCamera();
                fullscreenCam
                        .cap(cap)
                        .filter(f)
                        .fullscreen()
                        .run();
            } else {
                while (start && cap.grab()) {
                    cap.retrieve(buffer);
                    last = f.apply(buffer);
                    mat.setImage(mat2Image(last));
                }
            }
            stopStream(cap);


        }).start();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        options.addAll(FindFilters.findFilters());
        filters.setItems(options);
        f = mat -> mat;
        filters.getSelectionModel().selectedItemProperty().addListener((Observable, oldValue, newValue) -> {
            updateFilter();
        });
    }

    public void startStream(ActionEvent actionEvent) {
        if (!start) {
            startCamera();
        } else {
            // mat.setImage(mat2Image(new Mat(1, 1, CvType.CV_8UC3)));
        }
        start = !start;
    }

    public void stopStream(VideoCapture cap) {
        start = false;
        cap.release();
        message("Stream ended...");
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
            if (!StringUtils.isEmpty(current))
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
