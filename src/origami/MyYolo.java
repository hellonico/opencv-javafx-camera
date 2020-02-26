package origami;

import origami.filters.detect.Yolo;

public class MyYolo  {
    public static class V2 extends Yolo {
        public V2() {
            super("networks.yolo:yolov2-tiny:1.0.0");
        }
    }
    public static class V3 extends Yolo {
        public V3() {
            super("networks.yolo:yolov3-tiny:1.0.0");
        }
    }
}