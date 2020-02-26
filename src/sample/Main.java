package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import origami.Camera;
import origami.Origami;
import origami.filters.Sepia;

import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        URL fxml = Main.class.getResource("sample.fxml");
        Parent root = FXMLLoader.load(fxml);
        primaryStage.setTitle("Origami Cam");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }


    public static void main(String[] args) {
        Origami.init();
        launch(args);
    }
}
