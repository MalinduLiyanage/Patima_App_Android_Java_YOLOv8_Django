package com.onesandzeros.patima.prediction.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.onesandzeros.patima.R;
import com.onesandzeros.patima.SQLiteHelper;
import com.onesandzeros.patima.core.utils.UrlUtils;
import com.onesandzeros.patima.feedback.activity.FeedbackActivity;
import com.onesandzeros.patima.user.utils.ProfileManager;

public class ViewComparisonActivity extends AppCompatActivity {

    ImageView baseImg, processedImg;
    ImageButton feedbackBtn, homeBtn;
    String input_image_path, predicted_image_path;
    int predictionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_comparison);

        String userTypest = ProfileManager.getProfileRole(this);

        Intent intent = getIntent();
        if (intent != null) {
            input_image_path = intent.getStringExtra("input_image_path");
            predicted_image_path = intent.getStringExtra("predicted_image_path");
            predictionId = intent.getIntExtra("predictionId", 0);
        } else {
            Toast.makeText(this, "NULL", Toast.LENGTH_SHORT).show();
        }

        baseImg = findViewById(R.id.base_image);
        processedImg = findViewById(R.id.processed_image);
        feedbackBtn = findViewById(R.id.feedback_Btn);
        homeBtn = findViewById(R.id.home_Btn);

        if (userTypest.equals("General Public")) {
            feedbackBtn.setVisibility(View.GONE);
            feedbackBtn.setEnabled(false);
        }
        String input_image_full_path = UrlUtils.getFullUrl(input_image_path);
        String predicted_image_full_path = UrlUtils.getFullUrl(predicted_image_path);
        Glide.with(this).load(input_image_full_path).into(baseImg);
        Glide.with(this).load(predicted_image_full_path).into(processedImg);

        processedImg.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        baseImg.setVisibility(View.VISIBLE);
                        processedImg.setVisibility(View.GONE);
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        baseImg.setVisibility(View.GONE);
                        processedImg.setVisibility(View.VISIBLE);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        // Optional: You can add behavior for ACTION_MOVE if needed.
                        // For example, keep the base image visible while moving.
                        baseImg.setVisibility(View.VISIBLE);
                        processedImg.setVisibility(View.GONE);
                        return true;
                }
                return false;
            }
        });


        feedbackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewComparisonActivity.this, FeedbackActivity.class);
                intent.putExtra("imgId", 0);
                startActivity(intent);
            }
        });

        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
