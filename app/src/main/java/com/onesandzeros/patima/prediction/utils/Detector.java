package com.onesandzeros.patima.prediction.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.SystemClock;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.ops.CastOp;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Detector {

    private Context context;
    private String modelPath;
    private String labelPath;
    private DetectorListener detectorListener;

    private Interpreter interpreter;
    private List<String> labels = new ArrayList<>();

    private int tensorWidth = 0;
    private int tensorHeight = 0;
    private int numChannel = 0;
    private int numElements = 0;

    private final ImageProcessor imageProcessor = new ImageProcessor.Builder()
            .add(new NormalizeOp(INPUT_MEAN, INPUT_STANDARD_DEVIATION))
            .add(new CastOp(INPUT_IMAGE_TYPE))
            .build();

    public Detector(Context context, String modelPath, String labelPath, DetectorListener detectorListener) {
        this.context = context;
        this.modelPath = modelPath;
        this.labelPath = labelPath;
        this.detectorListener = detectorListener;
    }

    public List<String> getLabels() {
        return labels;
    }


    public void setup(Float confThreshold) {
        try {

            CONFIDENCE_THRESHOLD = confThreshold;

            ByteBuffer model = ByteBuffer.allocateDirect(FileUtil.loadMappedFile(context, modelPath).capacity())
                    .order(ByteOrder.nativeOrder());
            model.put(FileUtil.loadMappedFile(context, modelPath));

            Interpreter.Options options = new Interpreter.Options();
            options.setNumThreads(4);
            interpreter = new Interpreter(model, options);

            int[] inputShape = interpreter.getInputTensor(0).shape();
            int[] outputShape = interpreter.getOutputTensor(0).shape();

            tensorWidth = inputShape[1];
            tensorHeight = inputShape[2];
            numChannel = outputShape[1];
            numElements = outputShape[2];

            InputStream inputStream = context.getAssets().open(labelPath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                labels.add(line);
            }
            reader.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        if (interpreter != null) {
            //interpreter.close();
            interpreter = null;
        }
    }

    public void detect(Bitmap frame) {
        if (interpreter == null || tensorWidth == 0 || tensorHeight == 0 || numChannel == 0 || numElements == 0) return;

        if(frame == null){
            return;
        }

        long inferenceTime = SystemClock.uptimeMillis();

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(frame, tensorWidth, tensorHeight, false);

        TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
        tensorImage.load(resizedBitmap);
        TensorImage processedImage = imageProcessor.process(tensorImage);
        TensorBuffer output = TensorBuffer.createFixedSize(new int[]{1, numChannel, numElements}, OUTPUT_IMAGE_TYPE);

        interpreter.run(processedImage.getBuffer(), output.getBuffer());

        List<BoundingBox> bestBoxes = bestBox(output.getFloatArray());
        inferenceTime = SystemClock.uptimeMillis() - inferenceTime;

        if (bestBoxes == null) {
            detectorListener.onEmptyDetect();
        } else {
            //detectorListener.onDetect(bestBoxes, inferenceTime, frame);

            Map<String, Integer> labelCounts = new HashMap<>();
            for (BoundingBox box : bestBoxes) {
                String label = box.getClsName();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    labelCounts.put(label, labelCounts.getOrDefault(label, 0) + 1);
                }
            }

            int headCount = 0, bodyCount = 0;
            for (Map.Entry<String, Integer> entry : labelCounts.entrySet()) {
                String labelName = entry.getKey();
                if (labelName.contains("head")) {
                    headCount++;
                } else if (labelName.contains("body")) {
                    bodyCount++;
                }
            }

            Bitmap detectedBitmap = drawBoundingBoxes(frame.copy(Bitmap.Config.ARGB_8888, true), bestBoxes, labels);

            if (headCount == 0 && bodyCount == 1) {
                detectorListener.onDetect(bestBoxes, inferenceTime, frame, detectedBitmap,true);
            }else{
                detectorListener.onDetect(bestBoxes, inferenceTime, frame, detectedBitmap,false);
            }

        }
    }

    private Bitmap drawBoundingBoxes(Bitmap bitmap, List<BoundingBox> boundingBoxes, List<String> labels) {
        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8F);
        paint.setColor(Color.GREEN);
        paint.setTextSize(40);

        for (int i = 0; i < boundingBoxes.size(); i++) {
            BoundingBox box = boundingBoxes.get(i);
            float left = box.getX1() * mutableBitmap.getWidth();
            float top = box.getY1() * mutableBitmap.getHeight();
            float right = box.getX2() * mutableBitmap.getWidth();
            float bottom = box.getY2() * mutableBitmap.getHeight();

            canvas.drawRect(left, top, right, bottom, paint);

        }

        return mutableBitmap;
    }

    public void detectGallery(Bitmap frame) {
        if (interpreter == null || tensorWidth == 0 || tensorHeight == 0 || numChannel == 0 || numElements == 0) return;

        long inferenceTime = SystemClock.uptimeMillis();

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(frame, tensorWidth, tensorHeight, false);

        TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
        tensorImage.load(resizedBitmap);
        TensorImage processedImage = imageProcessor.process(tensorImage);
        TensorBuffer output = TensorBuffer.createFixedSize(new int[]{1, numChannel, numElements}, OUTPUT_IMAGE_TYPE);

        interpreter.run(processedImage.getBuffer(), output.getBuffer());

        List<BoundingBox> bestBoxes = bestBox(output.getFloatArray());
        inferenceTime = SystemClock.uptimeMillis() - inferenceTime;

        if (bestBoxes == null) {
            detectorListener.onEmptyDetectGallery();
        } else {

            Map<String, Integer> labelCounts = new HashMap<>();
            for (BoundingBox box : bestBoxes) {
                String label = box.getClsName();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    labelCounts.put(label, labelCounts.getOrDefault(label, 0) + 1);
                }
            }

            int headCount = 0, bodyCount = 0;
            for (Map.Entry<String, Integer> entry : labelCounts.entrySet()) {
                String labelName = entry.getKey();
                if (labelName.contains("head")) {
                    headCount++;
                } else if (labelName.contains("body")) {
                    bodyCount++;
                }
            }

            Bitmap detectedBitmap = drawBoundingBoxes(frame.copy(Bitmap.Config.ARGB_8888, true), bestBoxes, labels);

            if (headCount == 0 && bodyCount == 1) {
                detectorListener.onDetectGallery(bestBoxes, inferenceTime, detectedBitmap,true);
            }else{
                detectorListener.onDetectGallery(bestBoxes, inferenceTime, detectedBitmap,false);
            }
        }
    }

    private List<BoundingBox> bestBox(float[] array) {
        List<BoundingBox> boundingBoxes = new ArrayList<>();

        for (int c = 0; c < numElements; c++) {
            float maxConf = -1.0f;
            int maxIdx = -1;
            for (int j = 4, arrayIdx = c + numElements * j; j < numChannel; j++, arrayIdx += numElements) {
                if (array[arrayIdx] > maxConf) {
                    maxConf = array[arrayIdx];
                    maxIdx = j - 4;
                }
            }

            if (maxConf > CONFIDENCE_THRESHOLD) {
                String clsName = labels.get(maxIdx);
                float cx = array[c];
                float cy = array[c + numElements];
                float w = array[c + numElements * 2];
                float h = array[c + numElements * 3];
                float x1 = cx - (w / 2F);
                float y1 = cy - (h / 2F);
                float x2 = cx + (w / 2F);
                float y2 = cy + (h / 2F);
                if (x1 < 0F || x1 > 1F || y1 < 0F || y1 > 1F || x2 < 0F || x2 > 1F || y2 < 0F || y2 > 1F)
                    continue;

                boundingBoxes.add(new BoundingBox(x1, y1, x2, y2, cx, cy, w, h, maxConf, maxIdx, clsName));
            }
        }

        if (boundingBoxes.isEmpty()) return null;

        return applyNMS(boundingBoxes);
    }

    private List<BoundingBox> applyNMS(List<BoundingBox> boxes) {
        List<BoundingBox> sortedBoxes = new ArrayList<>(boxes);
        sortedBoxes.sort((box1, box2) -> Float.compare(box2.getCnf(), box1.getCnf()));
        List<BoundingBox> selectedBoxes = new ArrayList<>();

        while (!sortedBoxes.isEmpty()) {
            BoundingBox first = sortedBoxes.remove(0);
            selectedBoxes.add(first);

            sortedBoxes.removeIf(nextBox -> calculateIoU(first, nextBox) >= IOU_THRESHOLD);
        }

        return selectedBoxes;
    }

    private float calculateIoU(BoundingBox box1, BoundingBox box2) {
        float x1 = Math.max(box1.getX1(), box2.getX1());
        float y1 = Math.max(box1.getY1(), box2.getY1());
        float x2 = Math.min(box1.getX2(), box2.getX2());
        float y2 = Math.min(box1.getY2(), box2.getY2());
        float intersectionArea = Math.max(0F, x2 - x1) * Math.max(0F, y2 - y1);
        float box1Area = box1.getW() * box1.getH();
        float box2Area = box2.getW() * box2.getH();
        return intersectionArea / (box1Area + box2Area - intersectionArea);
    }

    public interface DetectorListener {
        void onEmptyDetect();
        void onDetect(List<BoundingBox> boundingBoxes, long inferenceTime, Bitmap detected, Bitmap detectedBitmap, boolean isautodetected);

        void onDetectGallery(List<BoundingBox> bestBoxes, long inferenceTime, Bitmap detectedBitmap, boolean isdetected);

        void onEmptyDetectGallery();
    }

    private static final float INPUT_MEAN = 0f;
    private static final float INPUT_STANDARD_DEVIATION = 255f;
    private static final DataType INPUT_IMAGE_TYPE = DataType.FLOAT32;
    private static final DataType OUTPUT_IMAGE_TYPE = DataType.FLOAT32;
    private static float CONFIDENCE_THRESHOLD = 0F;
    private static final float IOU_THRESHOLD = 0.5F;
}



