package origami.filters.detect;

import org.opencv.core.*;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;
import origami.Filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class YoloV8 implements Filter {
    Net net;
    List<String> layers;
    List<String> labels;
    float scoreThreshold = 0.9f;
    float nmsThreshold = 0.8f;

    public float getScoreThreshold() {
        return scoreThreshold;
    }

    public void setScoreThreshold(float scoreThreshold) {
        this.scoreThreshold = scoreThreshold;
    }

    public float getNmsThreshold() {
        return nmsThreshold;
    }

    public void setNmsThreshold(float nmsThreshold) {
        this.nmsThreshold = nmsThreshold;
    }

    List<String> getOutputsNames(Net net) {
        List<String> layersNames = net.getLayerNames();
        return net.getUnconnectedOutLayers().toList().stream().map(i -> i - 1).map(layersNames::get)
                .collect(Collectors.toList());
    }

    public YoloV8() {
        String spec = "networks.yolo:yolonas:1.0";
        List _net = origami.Dnn.readNetFromSpec(spec);
        net = (Net) _net.get(0);
        labels = (List<String>) _net.get(2);
        layers = getOutputsNames(net);
    }

    @Override
    public Mat apply(Mat in) {
        if (layers == null || layers.size() == 0) {
            return in;
        } else {
            findShapes(in);
            return in;
        }

    }

    final int IN_WIDTH = 640;
    final int IN_HEIGHT = 640;
    final double IN_SCALE_FACTOR = 0.00392157;
    final int MAX_RESULTS = 10;
    final boolean SWAP_RGB = true;
//    final String LABEL_FILE = "yolov3/coco.names";

    void findShapes(Mat frame) {

        Mat blob = Dnn.blobFromImage(frame, IN_SCALE_FACTOR, new Size(IN_WIDTH, IN_HEIGHT), new Scalar(128, 128, 128), SWAP_RGB);
        net.setInput(blob);

        List<Mat> outputs = new ArrayList<>();
        for (int i = 0; i < layers.size(); i++)
            outputs.add(new Mat());

        net.forward(outputs, layers);
        postprocess(frame, outputs);
    }

    private void postprocess(Mat frame, List<Mat> outs) {

        List<Rect2d> tmpLocations = new ArrayList<>();
        List<Integer> tmpClasses = new ArrayList<>();
        List<Float> tmpConfidences = new ArrayList<>();

        int w = frame.width();
        int h = frame.height();

        int ratiow = w / 640;
        int ratioh = h / 640;

        for (Mat out : outs) {
            out = out.reshape(1, 8400);
            int _h = out.height();
            int _w = out.width();

//            int k = 0;
            for (int j = 0; j < out.height(); j++) {

                Mat row = out.row(j);
                Mat scores = row.colRange(4, _w);
                Core.MinMaxLocResult result = Core.minMaxLoc(scores);
                if (result.maxVal > scoreThreshold) {
                    double center_x = row.get(0, 0)[0] * ratiow;
                    double center_y = row.get(0, 1)[0] * ratioh;
                    double width = row.get(0, 2)[0] * ratiow;
                    double height = row.get(0, 3)[0] * ratioh;
                    double x = center_x - width / 2;
                    double y = center_y - height / 2;
                    Rect2d rect = new Rect2d((int) x, (int) y, (int) width, (int) height);

                    tmpClasses.add((int) result.maxLoc.x);
                    tmpConfidences.add((float) result.maxVal);
                    tmpLocations.add(rect);

                }
//                k += out.width();
            }
        }

        annotateFrame(frame, tmpLocations, tmpClasses, tmpConfidences);
    }

    private void annotateFrame(Mat frame, List<Rect2d> tmpLocations, List<Integer> tmpClasses,
                               List<Float> tmpConfidences) {

        MatOfRect2d locMat = new MatOfRect2d();
        MatOfFloat confidenceMat = new MatOfFloat();
        MatOfInt indexMat = new MatOfInt();

        locMat.fromList(tmpLocations);
        confidenceMat.fromList(tmpConfidences);

        Dnn.NMSBoxes(locMat, confidenceMat, scoreThreshold, nmsThreshold, indexMat);

        try {
            processResults(frame, tmpLocations, tmpClasses, tmpConfidences, indexMat);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void processResults(Mat frame, List<Rect2d> tmpLocations, List<Integer> tmpClasses, List<Float> tmpConfidences, MatOfInt indexMat) {
        List<List> results = new ArrayList();
        for (int i = 0; i < indexMat.total() && i < MAX_RESULTS; ++i) {
            int idx = (int) indexMat.get(i, 0)[0];
            int labelId = tmpClasses.get(idx);
            Rect2d r_ = tmpLocations.get(idx);
            Rect box = new Rect(r_.tl(), r_.br());
            String label = labels.get(labelId) + "[" + String.format("%.0f", tmpConfidences.get(idx)) + "%]";
            results.add(Arrays.asList(box, label));
        }

        annotateAll(frame, results);
    }

    public void annotateAll(Mat frame, List<List> results) {
        for (List result : results) {
            annotateOne(frame, ((Rect) result.get(0)), (String) result.get(1));
        }
    }

    public void annotateOne(Mat frame, Rect box, String label) {
        Imgproc.rectangle(frame, box, new Scalar(0, 0, 0), 2);
        Imgproc.putText(frame, label, new Point(box.x, box.y), Imgproc.FONT_HERSHEY_PLAIN, 3.0, new Scalar(0, 0, 0), 2);
    }
}