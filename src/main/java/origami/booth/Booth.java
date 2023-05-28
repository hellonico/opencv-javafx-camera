package origami.booth;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import origami.Origami;

import java.awt.*;
import java.net.URL;

public class Booth extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        String fxmlFile = "/booth.fxml";
        Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));

        primaryStage.setTitle("Origami Photo Booth");
        // primaryStage.addEventHandler();

        Scene scene = new Scene(root, 1200, 600);
        primaryStage.setScene(scene);
//        Controller controller = fxmlLoader.getController();
//        JMetro jMetro = new JMetro(root, STYLE);
        scene.getStylesheets().add(Booth.class.getResource("/bootstrap3.css").toExternalForm());

        //primaryStage.getIcons().add(new Image("../icons/origami.png"));

        primaryStage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });

        setIcon("icons/booth.png");
        primaryStage.show();
    }

    public static void setIcon(String iconPath) {
        try {
            URL imageResource = Booth.class.getClassLoader().getResource(iconPath);
            Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
            java.awt.Image image = defaultToolkit.getImage(imageResource);
            Taskbar taskbar = Taskbar.getTaskbar();
            //set icon for mac os (and other systems which do support this method)
            taskbar.setIconImage(image);
        } catch (RuntimeException e) {
            System.out.println("The os does not support: 'taskbar.setIconImage'");
        }
    }

    public static void main(String[] args) throws ClassNotFoundException {
        Origami.init();
        // load youtube handler
//        Class.forName(YouTubeHandler.class.getName());

        launch(args);
    }
}
