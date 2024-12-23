package com.onesandzeros.patima.settings.activity;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.onesandzeros.patima.R;
import com.onesandzeros.patima.core.config.Config;
import com.onesandzeros.patima.prediction.utils.SegmentationModelHelper;

import java.io.File;
import java.io.IOException;

public class ParametersActivity extends AppCompatActivity {

    final private float RESET_DETECT_THRESHOLD = 0.6F;
    SharedPreferences sharedPreferences;
    ImageButton backBtn;
    private SeekBar thresholdSlider;
    private TextView thresholdValueText, segTxt, segSubTxt;
    private Switch segSwitch;
    private int SEGMENTATION_TYPE;
    private int RESET_SEGMENTATION_TYPE = 0;
    private ImageButton saveBtn, resetBtn;
    private float DETECT_THRESHOLD;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parameters);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        sharedPreferences = getSharedPreferences("patima", MODE_PRIVATE);

        File SegmentationmodelFile = SegmentationModelHelper.getModelFile(this, Config.SEG_MODEL_FILE_NAME);
        SEGMENTATION_TYPE = sharedPreferences.getInt("SEGMENTATION_TYPE", 0);
        segTxt = findViewById(R.id.seg_model_text);
        segSubTxt = findViewById(R.id.seg_model_subtext);
        segSwitch = findViewById(R.id.seg_model_switch);

        DETECT_THRESHOLD = sharedPreferences.getFloat("CONFIDENCE_THRESHOLD", 0);
        thresholdSlider = findViewById(R.id.thresholdbar);
        thresholdValueText = findViewById(R.id.thresholdval);

        saveBtn = findViewById(R.id.save_btn);
        resetBtn = findViewById(R.id.reset_btn);
        backBtn = findViewById(R.id.return_button);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putFloat("CONFIDENCE_THRESHOLD", DETECT_THRESHOLD);
                editor.putInt("SEGMENTATION_TYPE", SEGMENTATION_TYPE);
                editor.apply();
                Toast.makeText(ParametersActivity.this, "Changes Saved", Toast.LENGTH_SHORT).show();

            }
        });

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DETECT_THRESHOLD = RESET_DETECT_THRESHOLD;
                SEGMENTATION_TYPE = RESET_SEGMENTATION_TYPE;
                changeParameters();
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        segSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    segTxt.setText(Config.LOCAL_TEXT);
                    segSubTxt.setText(Config.LOCAL_SUB_TEXT);
                    SEGMENTATION_TYPE = 1;

                    if (!SegmentationmodelFile.exists()) {
                        progressDialog = new ProgressDialog(ParametersActivity.this);
                        progressDialog.setTitle("Downloading Segmentation Model...");
                        progressDialog.setMessage("Please wait while the model is being downloaded. It may take a few minutes. Model size is about 450 MB");
                        progressDialog.setCancelable(false);
                        progressDialog.show();

                        SegmentationModelHelper.downloadModelFile(Config.SEG_MODEL_URL, SegmentationmodelFile, new SegmentationModelHelper.DownloadCallback() {
                            @Override
                            public void onSuccess(File file) {
                                progressDialog.dismiss();

                            }
                            @Override
                            public void onError(String error) {
                                progressDialog.dismiss();
                                segTxt.setText(Config.CLOUD_TEXT);
                                segSubTxt.setText(Config.CLOUD_SUB_TEXT);
                                SEGMENTATION_TYPE = 0;
                                runOnUiThread(() -> {
                                    Toast.makeText(ParametersActivity.this, "Download failed: " + error, Toast.LENGTH_LONG).show();
                                });
                            }
                        });
                    } else {
                        Toast.makeText(ParametersActivity.this, "Model loaded from Downloads!", Toast.LENGTH_LONG).show();
                    }

                } else {
                    segTxt.setText(Config.CLOUD_TEXT);
                    segSubTxt.setText(Config.CLOUD_SUB_TEXT);
                    SEGMENTATION_TYPE = 0;
                }
            }
        });

        changeParameters();

    }

    private void changeParameters() {

        float initialThreshold = DETECT_THRESHOLD * 100;
        thresholdSlider.setProgress((int) initialThreshold);
        thresholdValueText.setText(String.valueOf(initialThreshold / 100));

        thresholdSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float currentProgress = (float) progress / seekBar.getMax(); // Convert progress to float
                thresholdValueText.setText(String.valueOf(currentProgress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Optional: handle touch start event (e.g., show a toast)
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                DETECT_THRESHOLD = (float) seekBar.getProgress() / seekBar.getMax();
            }
        });

        if(SEGMENTATION_TYPE == 0){
            segSwitch.setChecked(false);
            segTxt.setText(Config.CLOUD_TEXT);
            segSubTxt.setText(Config.CLOUD_SUB_TEXT);
        }else if(SEGMENTATION_TYPE == 1){
            segSwitch.setChecked(true);
            segTxt.setText(Config.LOCAL_TEXT);
            segSubTxt.setText(Config.LOCAL_SUB_TEXT);

        }

    }

}