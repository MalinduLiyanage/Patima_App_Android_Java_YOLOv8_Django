package com.onesandzeros.patima.prediction.activity;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.onesandzeros.patima.R;
import com.onesandzeros.patima.core.config.Config;
import com.onesandzeros.patima.core.network.ApiClient;
import com.onesandzeros.patima.prediction.model.Gps;
import com.onesandzeros.patima.prediction.model.NewPredictResponse;
import com.onesandzeros.patima.prediction.network.PredictionApiService;
import com.onesandzeros.patima.prediction.utils.SegmentationModelHelper;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProcessActivity extends AppCompatActivity {

    private final Handler handler = new Handler();
    private final String[] steps = {"Initializing...", "Segmenting...", "Identifying...", "Magic Logic...", "Completed"};
    LottieAnimationView lottie;
    TextView processTxt;
    String image_path, latitude, longitude;
    private int step = 0;
    private int SEGMENTATION_TYPE;
    SharedPreferences sharedPreferences;
    private Interpreter SegmentationInterpreter;
    private Bitmap segmentedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        lottie = findViewById(R.id.lottieAnimationView);
        processTxt = findViewById(R.id.process_txt);

        lottie.playAnimation();
        lottie.loop(true);

        updateText();

        Intent intent = getIntent();
        if (intent != null) {
            image_path = intent.getStringExtra("image_path");
            latitude = intent.getStringExtra("latitude");
            longitude = intent.getStringExtra("longitude");

        } else {
            Toast.makeText(this, "NULL", Toast.LENGTH_SHORT).show();
        }
        sharedPreferences = getSharedPreferences("patima", MODE_PRIVATE);
        SEGMENTATION_TYPE = sharedPreferences.getInt("SEGMENTATION_TYPE", 0);

        if (SEGMENTATION_TYPE == 0) {
            prediction();
        }else if (SEGMENTATION_TYPE == 1) {
            File imageFile = new File(image_path);
            if (imageFile.exists()) {
                segmentedImage = BitmapFactory.decodeFile(imageFile.getAbsolutePath());

                File SegmentationmodelFile = SegmentationModelHelper.getModelFile(this, Config.SEG_MODEL_FILE_NAME);
                try {
                    SegmentationInterpreter = new SegmentationModelHelper(SegmentationmodelFile).getInterpreter();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                offlineSegmentation();

            }else{
                Toast.makeText(this, "Image not found", Toast.LENGTH_SHORT).show();
            }
        }
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

            }
        };
        getOnBackPressedDispatcher().addCallback(this,onBackPressedCallback);


    }

    private void updateText() {
        if (step < steps.length) {
            processTxt.setText(steps[step]);
            applySlideAnimation();

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    step++;
                    updateText();
                }
            }, 4000);
        }
    }

    private void applySlideAnimation() {
        TranslateAnimation animate = new TranslateAnimation(-(processTxt.getWidth()), 0, 0, 0);
        animate.setDuration(500); // Animation duration
        processTxt.startAnimation(animate);
    }

    private void prediction(){
        if (image_path != null && latitude != null && longitude != null) {
            File file = new File(image_path);
            RequestBody reqFile = RequestBody.create(file, MediaType.parse("image/*"));
            Gps gps = new Gps(Double.parseDouble(longitude), Double.parseDouble(latitude));
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), reqFile);

            PredictionApiService predictionApiService = ApiClient.getClient(this).create(PredictionApiService.class);
            Call<NewPredictResponse> call = predictionApiService.newPrediction(body, gps);
            call.enqueue(new Callback<NewPredictResponse>() {
                @Override
                public void onResponse(Call<NewPredictResponse> call, Response<NewPredictResponse> response) {
                    if (response.isSuccessful()) {
                        NewPredictResponse newPredictResponse = response.body();
                        if (newPredictResponse != null) {
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(ProcessActivity.this, ViewComparisonActivity.class);
                                    intent.putExtra("newPrediction", newPredictResponse.getPrediction());
                                    intent.putExtra("isFeedback", true);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                    finish();
                                }
                            }, 20000);

                        }
                    } else {
                        Toast.makeText(ProcessActivity.this, "Failed to process image", Toast.LENGTH_SHORT).show();
                        finish();

                    }
                }

                @Override
                public void onFailure(Call<NewPredictResponse> call, Throwable t) {
                    Toast.makeText(ProcessActivity.this, "Failed to process image", Toast.LENGTH_SHORT).show();
                    Log.e("Error", t.getMessage());
                    finish();
                }
            });
        }
    }

    private void offlineSegmentation() {

        if (segmentedImage == null || SegmentationInterpreter == null) return;

        new Thread(() -> {
            float[][][][] input = SegmentationModelHelper.preprocessImage(segmentedImage, 256, 256);
            float[][][][] output = new float[1][256][256][1];

            SegmentationInterpreter.run(input, output);

            Bitmap resultBitmap = SegmentationModelHelper.postProcessMask(output, 256, 256, segmentedImage);

            runOnUiThread(() -> {

                File photoFile = new File(getOutputDirectory(), new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + "_segmented.jpg");

                try (FileOutputStream fos = new FileOutputStream(photoFile)) {
                    resultBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.flush();
                    image_path =  photoFile.getAbsolutePath();
                    Log.d(TAG,image_path);
                    prediction();
                } catch (IOException e) {
                    Log.e(TAG, "Error saving bitmap", e);
                }

            });
        }).start();
    }

    private File getOutputDirectory() {
        File mediaDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "tempCaptures");
        if (!mediaDir.exists()) {
            mediaDir.mkdirs();
        }
        return mediaDir;
    }


}