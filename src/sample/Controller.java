package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import origami.Filter;
import origami.Filters;
import origami.filters.FPS;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    private ImageView mat;
    boolean start=false;

    @FXML
    private ComboBox<String> filters;

    @FXML
    CheckBox fps;

    ObservableList<String> options =
            FXCollections.observableArrayList();


    private Image mat2Image(Mat frame)
    {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", frame, buffer);
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }

    Filter f;
    public void startCamera() {
        new Thread(()-> {
            VideoCapture cap = new VideoCapture(0);
            Mat var1 = new Mat();
            while(start && cap.grab()) {
                cap.retrieve(var1);
                mat.setImage(mat2Image(f.apply(var1)));
            }
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
        if(!start) {
            startCamera();
        } else {
//            mat.imageProperty().set(null);
        }
        start=!start;
    }

    public void check(ActionEvent actionEvent) {
        updateFilter();
    }

    private void updateFilter() {

        if(fps.isSelected()) {
            f = new Filters(getCurrentFilter(), new FPS());
        } else {
            f = getCurrentFilter();
        }
    }

    private Filter getCurrentFilter() {
        String current = filters.getValue();
        try {
            return (Filter) Class.forName(current).newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return f->f;
        }
    }
}
