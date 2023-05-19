package origami.booth;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import origami.Origami;
import origami.video.YouTubeHandler;

import java.awt.*;
import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("booth.fxml").openStream());
        primaryStage.setTitle("Origami Photo Booth");
        Scene scene = new Scene(root, 1000, 600);
        primaryStage.setScene(scene);
//        Controller controller = fxmlLoader.getController();
//        JMetro jMetro = new JMetro(root, STYLE);
        scene.getStylesheets().add(Main.class.getResource("/bootstrap3.css").toExternalForm());

        //primaryStage.getIcons().add(new Image("../icons/origami.png"));

        setIcon();
        primaryStage.show();
    }

    public void setIcon() {

        try {

            Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
            URL imageResource = Main.class.getClassLoader().getResource("icons/origami.png");
            java.awt.Image image = defaultToolkit.getImage(imageResource);
            Taskbar taskbar = Taskbar.getTaskbar();

            //set icon for mac os (and other systems which do support this method)
            taskbar.setIconImage(image);
        } catch (
                final UnsupportedOperationException e) {
            System.out.println("The os does not support: 'taskbar.setIconImage'");
        } catch (
                final SecurityException e) {
            System.out.println("There was a security exception for: 'taskbar.setIconImage'");
        }
    }


    public static void main(String[] args) throws ClassNotFoundException {
        Origami.init();
        // load youtube handler
        Class.forName(YouTubeHandler.class.getName());

        launch(args);
    }
}
