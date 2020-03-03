package origami;

import org.opencv.core.Mat;
import org.opencv.video.Video;
import org.opencv.videoio.VideoCapture;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class StoppableCamera extends Camera {

    static boolean stop = false;
    static {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            stop = true;
            return true;
        });
    }

    private boolean fullscreen = false;

    public StoppableCamera() {
        super();
    }

    public Camera fullscreen() {
        this.fullscreen = true;
        return super.fullscreen();
    }
    
    @Override
    public void run() {
        stop = false;
        if (this.cap == null) {
            this.cap = new VideoCapture(0);
        }

        Mat var1 = new Mat();

        while (!stop && this.cap.grab()) {
            this.cap.retrieve(var1);
            this.ims.showImage((Mat) this.filter.apply(var1));
        }

        this.cap.release();

        if(this.fullscreen)
            exitFullScreen();
    }

    private void exitFullScreen() {
        this.ims.Window.setVisible(false);
//        this.ims.Window.dispose();
        GraphicsDevice var3 = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        var3.setFullScreenWindow(null);
    }


    public Camera cap(VideoCapture _cap) {
        this.cap = _cap;
        return this;
    }
}
