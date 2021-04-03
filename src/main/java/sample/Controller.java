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
import origami.FindFilters;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import origami.*;
import origami.filters.FPS;
import origami.utils.FileWatcher;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static origami.Origami.FilterToString;
import static origami.Origami.StringToFilter;

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
    TextField custom;

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

    private boolean streamToJpg = false;

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
    Camera fullscreenCam;


    public VideoCapture getVideoCapture() {
        try {
            String _vid = vid.getText();
            return Origami.CaptureDevice(_vid);
        } catch (Exception e) {
            message(e.getMessage());
            start = false;
            throw new RuntimeException(e);
        }
    }

    public void startCamera() {
        new Thread(() -> {

            VideoCapture cap = getVideoCapture();

            if (fullscreen.isSelected()) {
                fullscreenCam = new Camera();
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

        if (streamToJpg)
            new Thread(() -> {
                while (start) {
                    imwrite("stream.jpg", last);
                }
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
            Filter _f = (Filter) klass.newInstance();
            String label = FilterToString(_f);
            custom.setText(label);
            return _f;
        } catch (Exception e) {
            if (!"".equals(current) && current != null)
                message("Can't load:" + current);
            return f -> f;
        }
    }

    public void takeShot(ActionEvent actionEvent) {
        String file = new Date().toString() + ".png";
        imwrite(file, last);
        message(file + " was saved");
    }

    public void fullscreenClick(ActionEvent actionEvent) {
        startCamera();
    }


    public class MyFileWatcher extends FileWatcher {
        public MyFileWatcher(File watchFile) {
            super(watchFile);
        }

        @Override
        public void doOnChange() {
            keyType();
        }

    }

    MyFileWatcher fw;

    public void keyType() {
        String customFilter = custom.getText();
        File f = new File(customFilter);
        if (fw != null) fw.stopThread();
        if (f.exists()) {
            Filter fi = StringToFilter(f);
            this.f = fi;
            fw = new MyFileWatcher(f);
            fw.start();
            message("Filter loaded:" + f.getName());
        }
        try {
            Filter _f = StringToFilter(customFilter);
            this.f = _f;
            message("Filter updated:" + FilterToString(_f));
        } catch (Exception e) {
            // unbound
            // e.printStackTrace();
        }
    }

}
