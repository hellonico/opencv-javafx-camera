package sample;

import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import origami.Origami;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
//        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("sample.fxml"));
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
