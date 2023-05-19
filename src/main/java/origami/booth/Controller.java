package origami.booth;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import org.opencv.videoio.VideoWriter;
import origami.FindFilters;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import origami.*;
//import origami.filters.Detect;
import origami.filters.FPS;
import origami.utils.FileWatcher;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
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

    public ToggleButton togglerecordButton;
    @FXML
    public TextArea projectName;

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

    @FXML
    TextArea custom;

    @FXML
    TextArea detector;

    @FXML
    TextField vid;

    @FXML
    TextArea message;

    @FXML
    TextField width;

    @FXML
    TextField height;

    ObservableList<String> options = FXCollections.observableArrayList();
    Mat last = new Mat();

    @FXML
    ToggleButton fullscreen;

    Detect detectorObject;

    ArrayList<Function<String, String>> onShotHandlers = new ArrayList();
    private Session session = new Session();

    Camera internalCamObject = new Camera();


    File lastFile;


    public void setupInternalCam() {
        // internalCamObject.ims.setCloseOption(HIDE_ON_CLOSE);
        internalCamObject.slowDown(100);
        internalCamObject.setFn(new BoothCameraFn(this));

    }

    public Controller() {
        super();
        // register shot handlers
        setupInternalCam();

        // set last file
        onShotHandlers.add(_file -> {
            this.lastFile = new File(_file);
            return _file;
        });
        // set preview image
        onShotHandlers.add(_file -> {
            this.preview.setImage(OrigamiFX.file2FXImage(new File(_file)));
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



    private void message(String e) {
        System.out.println(e);
        message.appendText(e);
        message.appendText("\n");
    }

    public void restartStream() {
        internalCamObject.stop();
        startCamera();
    }

    public void startCamera() {
        message("Streaming...");
        new Thread(() -> {

            // somewhere else
            String _vid = vid.getText();
            internalCamObject.cap(Origami.CaptureDevice(_vid));

            internalCamObject.run();
            this.start = false;
            // comes here when finished.
            // stopStream(this.internalCamObject.VC());
            // stopRecording if it was started
            togglerecordButton.setSelected(false);

        }).start();

    }

    private synchronized void startRecordingThread() {

        new Thread(() -> {

            message("start recording with size: " + last.size());
            VideoWriter vw = new VideoWriter();

            String filename = "stream_" + LocalDateTime.now() + ".mp4";

            // https://softron.zendesk.com/hc/en-us/articles/207695697-List-of-FourCC-codes-for-video-codecs
            // slow but compatible ?
            String code = "mjpg";
            // fast but only for osx
            // TODO: should in settings
            // String code = "avc1";
            int fourcc = VideoWriter.fourcc(code.charAt(0), code.charAt(1), code.charAt(2), code.charAt(3));
            vw.open(filename, fourcc, 240, last.size());

            while (togglerecordButton.isSelected()) {
                if (start) {
                    vw.write(last);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            message("recording stopped");
            vw.release();
        }).start();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        options.addAll(FindFilters.findFilters());
        filters.setItems(options);
        filters.getSelectionModel().selectedItemProperty().addListener((Observable, oldValue, newValue) -> {
            updateFilter();
        });
    }

    public void startStream(ActionEvent actionEvent) {
        start = !start;
        if (start) {
            startCamera();
        } else {
            internalCamObject.stop();
            message("Stream ended...");
        }
    }
//
//    public void stopStream(VideoCapture cap) {
//        internalCamObject.stop();
//
//        start = false;
////        cap.release();
//
//    }

    public void check(ActionEvent actionEvent) {
        updateFilter();
    }

    private void updateFilter() {
        Filter f = mat -> mat;
        if (fps.isSelected()) {
            f = new Filters(getFilterFromCustomEditText(), new FPS());
        } else {
            f = getFilterFromCustomEditText();
        }
        this.internalCamObject.filter(f);
    }

    private Filter getFilterFromCustomEditText() {
        String customString = custom.getText();
        Filter _f = Origami.StringToFilter(customString);
        String label = FilterToString(_f);
        return _f;
    }

    // this is in camera
    public void takeShot(ActionEvent actionEvent) {
        String file = new Date().toString() + ".png";
        imwrite(file, last.clone());
        message(file + " was saved");
        for (Function f : onShotHandlers) {
            f.apply(file);
        }
    }

    public void fullscreenClick(ActionEvent actionEvent) {
        // startCamera();
        this.internalCamObject.headless();
        this.internalCamObject.fullscreen();

        message("HEADLESS:"+this.internalCamObject.isHeadless());
        if(this.internalCamObject.isHeadless()) {
            this.internalCamObject.ims.Window.setVisible(false);
            this.internalCamObject.setFn(new BoothCameraFn(this));
        } else {
            this.internalCamObject.setFn(new DefaultCameraFn());
        }
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
        boolean streamToJpg = togglerecordButton.isSelected();
        if (streamToJpg) {
            streamToJpg = true;
            message("Recording is ON");
            startRecordingThread();
        } else {
            streamToJpg = false;
            message("Recording is OFF");
        }
    }

    public void saveProject() {
        File projectFolder = new File(this.projectName.getText());
        projectFolder.mkdirs();
        session.saveSession(projectName.getText(), "cam.edn", vid);
        session.saveSession(projectName.getText(), "filters.edn", custom);
    }

    public void loadProject() {
        // TODO: create projects
        session.loadSession(projectName.getText(), "cam.edn", vid);
        session.loadSession(projectName.getText(), "filters.edn", custom);

        updateFilter();
//        restartStream();
    }

    public void newSelectedFilter(ActionEvent actionEvent) {
        try {
            // get the filter from the class name
            Class flass = Class.forName(filters.getValue());
            Filter _f = (Filter) flass.newInstance();
            custom.setText(Origami.FilterToString(_f));
            updateFilter();
        } catch (Exception e) {
            e.printStackTrace();
            message(e.getMessage());
        }
    }

    public class MyFileWatcher extends FileWatcher {
        public MyFileWatcher(File watchFile, TextArea custom) {
            super(watchFile);
        }

        @Override
        public void doOnChange() {
            try {
                custom.setText(Files.readString(Path.of(this.file.toURI())));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }


    }

    MyFileWatcher fw;

    public void filterTextKeyType() {

        String customFilter = custom.getText();

        File f = new File(customFilter);
        if (f.exists()) {
            if (fw != null) {
                fw.stopThread();
            }

            fw = new MyFileWatcher(f, custom);
            fw.start();
            message("Starting watcher:" + f.getName());
        } else {
            updateFilter();
        }

    }

}
