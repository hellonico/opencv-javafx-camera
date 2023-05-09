package origami.booth;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import org.opencv.video.Video;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;
import origami.FindFilters;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import origami.*;
//import origami.filters.Detect;
import origami.filters.FPS;
import origami.utils.FileWatcher;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.function.Function;

import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static origami.Origami.FilterToString;
import static origami.Origami.StringToFilter;

public class Controller implements Initializable {

    @FXML
    ImageView preview;

    @FXML
    ImageView detected;

    @FXML
    ImageView mat;
    boolean start = false;

    @FXML
    ComboBox<String> filters;

    @FXML
    ToggleButton fps;
    //
    // @FXML
    // Button stream;
    @FXML
    TextArea custom;

    @FXML
    TextArea detector;

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
    ToggleButton fullscreen;

    private boolean streamToJpg = false;
    private Detect detectorObject;

    private Image mat2FXImage(Mat frame) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", frame, buffer);
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }

    private Image file2FXImage(File imageFile) {
        String fileLocation = imageFile.toURI().toString();
        return new Image(fileLocation);
    }

    ArrayList<Function<String, String>> onShotHandlers = new ArrayList();

    public Controller() {
        super();
        // register shot handlers

        // set last file
        onShotHandlers.add(_file -> {
            this.lastFile = new File(_file);
            return _file;
        });
        // set preview image
        onShotHandlers.add(_file -> {
            this.preview.setImage(file2FXImage(new File(_file)));
            return _file;
        });
        // copy to clipboard
        onShotHandlers.add(_file -> {
            StringSelection stringSelection = new StringSelection(new File(_file).getAbsolutePath());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
            this.message("photo copied to clipboard");
            return _file;
        });
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
        message("Streaming...");
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
                    try {
                        last = f.apply(buffer).clone();
                    } catch (Exception e) {
                        last = buffer.clone();
                        e.printStackTrace();
                    }
                    mat.setImage(mat2FXImage(last));

                    if(this.detectorObject!=null) {
                        if(detectorObject.detected(last)) {
                            this.preview.setImage(mat2FXImage(detectorObject.detectMats(last.clone()).get(0)));
                        } else {
                            this.preview.setImage(null);
                        }
                    }
                }
            }
            stopStream(cap);

        }).start();

        if (streamToJpg) {
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    // e.printStackTrace();
                }
                message("start recording with size: "+last.size());
                VideoWriter vw = new VideoWriter();
                String filename = "stream_" + LocalDateTime.now() + ".mp4";
                vw.open(filename, Videoio.CAP_PROP_FOURCC,5.0 ,last.size());

                while (start) {
                    vw.write(last);
                }
                vw.release();
            }).start();
        }

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


    File lastFile;

    public void takeShot(ActionEvent actionEvent) {
        String file = new Date().toString() + ".png";
        imwrite(file, last.clone());
        message(file + " was saved");
        for (Function f : onShotHandlers) {
            f.apply(file);
        }
    }

    public void fullscreenClick(ActionEvent actionEvent) {
        startCamera();
    }

    public void takeShotTimer(ActionEvent actionEvent) {
        new Thread(() -> {
            try {
                Thread.sleep(10_000);
                this.takeShot(actionEvent);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void openFile(MouseEvent mouseEvent) {
        if (lastFile == null) return;
        Desktop desktop = Desktop.getDesktop();
        try {
            desktop.open(lastFile);
        } catch (IOException e) {
            System.out.println("Can't open last shot");
        }
    }

    public void keyTypeDetecor(KeyEvent keyEvent) {
        String customFilter = detector.getText();
        try {
            Filter _f = StringToFilter(customFilter);
            this.detectorObject = (Detect) _f;
            message("Detector updated:" + detectorObject.getClass());
        } catch (Exception e) {
            this.detectorObject = null;
            message(e.getMessage());
        }
    }

    public void detectFrontal(ActionEvent actionEvent) {
        this.detector.setText("{:class origami.filters.detect.Haar :type \"haar.frontal\"}");
        this.keyTypeDetecor(null);
    }

    public void togglerecord(ActionEvent actionEvent) {
        streamToJpg = !streamToJpg;
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
            message(e.getMessage());
            // e.printStackTrace();
            // unbound
            // e.printStackTrace();
        }
    }

}
