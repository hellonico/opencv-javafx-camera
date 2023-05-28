package origami.booth;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import org.opencv.core.Mat;
import origami.*;
import origami.filters.FPS;
import origami.utils.FileWatcher;
import origami.utils.Resourcer;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.*;
import java.util.function.Function;

import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static origami.Origami.FilterToString;
import static origami.Origami.StringToFilter;

public class Controller implements Initializable {

    public ToggleButton togglerecordButton;
    @FXML
    public TextArea projectName;
    public SplitPane panel;

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
//        internalCamObject.slowDown(100);
        internalCamObject.setFn(new BoothCameraFn(this));

    }

    public Controller() {
        super();
        // register shot handlers
        setupInternalCam();

        // register key mappings
        try {
            loadKeyMapping();
        } catch (Exception e) {
            e.printStackTrace();
            // throw new RuntimeException(e);
        }

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
            internalCamObject.run();

            // this.start = false;

            togglerecordButton.setSelected(false);

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

    public void startStream() {
        start = !start;
        if (start) {
            startCamera();
        } else {
            internalCamObject.stop();
            message("Stream ended...");
        }
    }

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

        message("HEADLESS:" + this.internalCamObject.isHeadless());
        if (this.internalCamObject.isHeadless()) {
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
        OrigamiFX.desktopOpenFile(lastFile.getAbsolutePath());
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
        internalCamObject.record();
        if (togglerecordButton.isSelected()) {
            message("Recording is ON");
        } else {
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
        updateDevice();
    }

    public void newSelectedFilter(ActionEvent actionEvent) {
        try {
            Class flass = Class.forName(filters.getValue());
            Filter _f = (Filter) flass.newInstance();
            custom.setText(Origami.FilterToString(_f));
            updateFilter();
        } catch (Exception e) {
            message(e.getMessage());
        }
    }

    public void updateDevice() {
        message("New Device Info Loaded");
        String _vid = vid.getText();
        internalCamObject.device(_vid);
        if (start) {
            restartStream();
        }
    }

    public void resize(MouseEvent mouseEvent) {
        // mat.getParent().layout();
    }

    public void loadKeyMapping() throws Exception {

        String text = Resourcer.linesFromResourceAndCustomization("mappings.json");

        JSONObject root = JSON.parseObject(text);
        for (String filterName : root.keySet()) {
            JSONObject map = (JSONObject) root.get(filterName);

            HashMap keysForFilter = new HashMap();
            for (String key : map.keySet()) {
                JSONArray array = (JSONArray) map.get(key);
                String klass = (String) array.get(0);
                array.remove(0);
                Constructor c = Class.forName("origami.booth.FilterAction$" + klass).getConstructors()[0];
                keysForFilter.put(key, c.newInstance((String[]) array.toArray(String[]::new)));
            }
            mappings.put(filterName, keysForFilter);
        }
    }

    HashMap mappings = new HashMap();

    public void pressMe(KeyEvent keyEvent) throws Exception {


        if (keyEvent.getText().equalsIgnoreCase("@")) {
            int current = (int) Math.round(panel.getDividerPositions()[0]);
            panel.setDividerPosition(0, 1 - current);
            return;
        }

        Filter f = this.internalCamObject.getFilter();
        String CurrentFilterName = f.getClass().getSimpleName();

        ((FilterAction) ((Map) mappings.get(CurrentFilterName)).get(keyEvent.getText())).apply(f);

        custom.setText(Origami.FilterToString(f));

    }

    public class WatcherForFilter extends FileWatcher {
        public WatcherForFilter(File watchFile) {
            super(watchFile);
        }

        public void doOnChangeContent(String content) {
            custom.setText(content);
        }

    }

    WatcherForFilter fw;

    public void filterTextKeyType() {

        String customFilter = custom.getText();

        File f = new File(customFilter);
        if (f.exists()) {
            if (fw == null) {
                fw = new WatcherForFilter(f);
                fw.start();
                message("Started watcher:" + f.getName());
            } else {
                if (!f.getAbsolutePath().equalsIgnoreCase(fw.getFile().getAbsolutePath())) {
                    fw.setFile(f);
                }
            }
            message("Watching:" + f.getName());

        } else {
            if (fw != null)
                fw.stopThread();
            updateFilter();
        }

    }
}