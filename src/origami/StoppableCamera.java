package origami;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class StoppableCamera extends Camera {

    static boolean stop = false;

    public StoppableCamera() {
        super();
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            System.out.println("Got key event!:"+stop);
            this.setStop(true);
            return true;
        });

    }

    public void setStop(boolean _stop) {
        System.out.println("set stop!:"+stop);
        this.stop = _stop;
    }

    @Override
    public void run() {
        System.out.println("run...");
        if (this.cap == null) {
            this.cap = new VideoCapture(0);
        }

        Mat var1 = new Mat();

        while (!this.stop && this.cap.grab()) {
            System.out.println("run..."+stop);
            this.cap.retrieve(var1);
            this.ims.showImage((Mat) this.filter.apply(var1));
        }

        cap.release();
        this.ims.Window.setVisible(false);

    }
}
