package origami.booth;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import origami.Origami;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("sample.fxml").openStream());
        primaryStage.setTitle("Origami Cam");
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
//        Controller controller = fxmlLoader.getController();
//        JMetro jMetro = new JMetro(root, STYLE);
        scene.getStylesheets().add(Main.class.getResource("/bootstrap3.css").toExternalForm());

        primaryStage.show();
    }

    public static void main(String[] args) {
        Origami.init();
        launch(args);
    }
}
