package origami.booth;

import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputControl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Session {


    public void loadSession(String project, String item, TextInputControl input) {

        if (getFile(project, item).exists()) {
            String content = null;
            try {
                content = Files.readString(Paths.get(getFile(project, item).getPath()), StandardCharsets.UTF_8);
                input.setText(content);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private File getFile(String project, String item) {
        return new File(project + "/" + item);
    }

    public void saveSession(String project, String item, TextInputControl custom) {
        File content = getFile(project, item);
        try {
            Path path = Paths.get(getFile(project, item).getPath());
            Files.writeString(path, custom.getText(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
