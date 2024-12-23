package com.onesandzeros.patima.prediction.utils;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class SegmentationModelHelper {
    private Interpreter tflite;

    public SegmentationModelHelper(File modelFile) throws IOException {
        MappedByteBuffer tfliteModel = loadModelFile(modelFile);
        tflite = new Interpreter(tfliteModel);
    }

    private MappedByteBuffer loadModelFile(File modelFile) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(modelFile);
        FileChannel fileChannel = fileInputStream.getChannel();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, modelFile.length());
    }

    public Interpreter getInterpreter() {
        return tflite;
    }

    public static Bitmap postProcessMask(float[][][][] mask, int width, int height, Bitmap selectedBitmap) {
        Bitmap maskBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Bitmap resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float value = mask[0][y][x][0];
                int binaryValue = value > 0.5 ? 255 : 0;
                int maskPixelValue = 0xFF << 24 | binaryValue << 16 | binaryValue << 8 | binaryValue;
                maskBitmap.setPixel(x, y, maskPixelValue);
                int originalPixel = selectedBitmap.getPixel(x, y);
                if (binaryValue == 0) {
                    resultBitmap.setPixel(x, y, 0xFF000000);
                } else {
                    resultBitmap.setPixel(x, y, originalPixel);
                }
            }
        }

        return resultBitmap;
    }

    public static float[][][][] preprocessImage(Bitmap bitmap, int targetWidth, int targetHeight) {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, false);
        float[][][][] input = new float[1][targetHeight][targetWidth][3];

        for (int y = 0; y < targetHeight; y++) {
            for (int x = 0; x < targetWidth; x++) {
                int pixel = resizedBitmap.getPixel(x, y);
                input[0][y][x][0] = ((pixel >> 16) & 0xFF) / 255.0f;
                input[0][y][x][1] = ((pixel >> 8) & 0xFF) / 255.0f;
                input[0][y][x][2] = (pixel & 0xFF) / 255.0f;
            }
        }
        return input;
    }

    public static File getModelFile(Context context, String modelFileName) {
        File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        return new File(dir, modelFileName);
    }

    public static void downloadModelFile(String fileUrl, File destination, DownloadCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(fileUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    callback.onError("Server returned HTTP " + connection.getResponseCode());
                    return;
                }

                InputStream inputStream = connection.getInputStream();
                FileOutputStream outputStream = new FileOutputStream(destination);

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                outputStream.close();
                inputStream.close();
                callback.onSuccess(destination);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }
    public interface DownloadCallback {
        void onSuccess(File file);
        void onError(String error);
    }
}
