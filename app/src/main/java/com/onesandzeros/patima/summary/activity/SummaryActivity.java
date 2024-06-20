package com.onesandzeros.patima.summary.activity;

import static java.lang.Math.cos;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.UserManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.onesandzeros.patima.LocationAdapter;
import com.onesandzeros.patima.R;
import com.onesandzeros.patima.SQLiteHelper;
import com.onesandzeros.patima.core.network.ApiClient;
import com.onesandzeros.patima.core.utils.UrlUtils;
import com.onesandzeros.patima.feedback.activity.FeedbackActivity;
import com.onesandzeros.patima.feedback.adapter.FeedbackAdapter;
import com.onesandzeros.patima.feedback.model.Feedback;
import com.onesandzeros.patima.feedback.network.FeedbackApiService;
import com.onesandzeros.patima.feedback.network.FeedbackResponse;
import com.onesandzeros.patima.prediction.model.Location;
import com.onesandzeros.patima.user.utils.ProfileManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SummaryActivity extends AppCompatActivity {

    private static final double EARTH_RADIUS_KM = 6371.0;
    String basePath = null, detectionPath = null, timeStamp = null;
    int imgId = 0;
    FloatingActionButton feedbackBtn;
    TextView feedbackTextShow, detectTxt, nearbydetectTxt;
    RecyclerView feedbackContainer, nearbyContainer;
    SQLiteHelper dbHelper;
    double selectedImg_lat, selectedImg_lon;
    private ImageView baseImg;
    private ImageView processedImg;
    private List<Feedback> feedbackList;
    private FeedbackAdapter feedbackAdapter;
    private List<Location> locationList;
    private LocationAdapter locationAdapter;

    public static boolean isWithinRadius(double lat, double lon, double targetLat, double targetLon, double radiusKm) {
        double deltaLat = toDegrees(radiusKm / EARTH_RADIUS_KM);
        double deltaLon = toDegrees(radiusKm / (EARTH_RADIUS_KM * cos(toRadians(lat))));

        double minLat = lat - deltaLat;
        double maxLat = lat + deltaLat;
        double minLon = lon - deltaLon;
        double maxLon = lon + deltaLon;

        return targetLat >= minLat && targetLat <= maxLat && targetLon >= minLon && targetLon <= maxLon;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        imgId = getIntent().getIntExtra("imgId", 0);
        basePath = getIntent().getStringExtra("base_path");
        detectionPath = getIntent().getStringExtra("detection_path");
        //timeStamp = getIntent().getStringExtra("timestamp");

        baseImg = findViewById(R.id.base_image);
        processedImg = findViewById(R.id.processed_image);
        feedbackBtn = findViewById(R.id.btn_feedback);
        feedbackTextShow = findViewById(R.id.feedbackshow);
        feedbackContainer = findViewById(R.id.feedback_container);
        nearbyContainer = findViewById(R.id.nearby_container);
        detectTxt = findViewById(R.id.detect_txt);
        nearbydetectTxt = findViewById(R.id.nearby_detect_txt);


        String userTypest = ProfileManager.getProfileRole(this);

        if (userTypest.equals("General Public User")) {
            feedbackBtn.setVisibility(View.INVISIBLE);
            feedbackTextShow.setVisibility(View.GONE);
            feedbackContainer.setVisibility(View.GONE);
            feedbackBtn.setEnabled(false);

        } else {
//            loaduserFeedbacks();
        }

        loadImages();
//        loadNearby(imgId);
//        nearbyImages();

        feedbackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SummaryActivity.this, FeedbackActivity.class);
                intent.putExtra("imgId", imgId);
                startActivity(intent);
            }
        });

    }

    private void loadNearby(int imgId) {
        dbHelper = new SQLiteHelper(this);
        String location = dbHelper.getImagetag(imgId);
        String[] locationParts = location.split(", ");
        selectedImg_lat = Double.parseDouble(locationParts[0]);
        selectedImg_lon = Double.parseDouble(locationParts[1]);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences("Startup", MODE_PRIVATE);
        String userTypest = sharedPreferences.getString("userType", "");
        int userid = sharedPreferences.getInt("userId", 0);
        if (userTypest.equals("General Public User")) {
            feedbackBtn.setVisibility(View.INVISIBLE);
            feedbackTextShow.setVisibility(View.GONE);
            feedbackContainer.setVisibility(View.GONE);
            feedbackBtn.setEnabled(false);

        } else {
            loadPredictedFeedbacks();
        }
    }

    private void loadPredictedFeedbacks() {
        FeedbackApiService feedbackApiService = ApiClient.getClient(this).create(FeedbackApiService.class);
        Call<FeedbackResponse> call = feedbackApiService.retrieveFeedbacksByPredId(imgId);
        call.enqueue(new Callback<FeedbackResponse>() {
            @Override
            public void onResponse(Call<FeedbackResponse> call, Response<FeedbackResponse> response) {
                if (response.isSuccessful()) {
                    feedbackList = response.body().getFeedbacks();
                    feedbackAdapter = new FeedbackAdapter(feedbackList, SummaryActivity.this, true);
                    feedbackContainer.setLayoutManager(new LinearLayoutManager(SummaryActivity.this));
                    feedbackContainer.setAdapter(feedbackAdapter);
                }
            }

            @Override
            public void onFailure(Call<FeedbackResponse> call, Throwable t) {

            }
        });


    }

    private void loadImages() {

        processedImg.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        baseImg.setVisibility(View.VISIBLE);
                        processedImg.setVisibility(View.GONE);
                        return true;

                    case MotionEvent.ACTION_UP:
                        baseImg.setVisibility(View.GONE);
                        processedImg.setVisibility(View.VISIBLE);
                        return true;
                }
                return false;
            }
        });
        String fullDetectionPath = UrlUtils.getFullUrl(detectionPath);
        String fullBasePath = UrlUtils.getFullUrl(basePath);
        Glide.with(this)
                .load(fullDetectionPath)
                .into(processedImg);

        Glide.with(this)
                .load(fullBasePath)
                .into(baseImg);


    }

    private void nearbyImages() {
        dbHelper = new SQLiteHelper(this);
        locationList = new ArrayList<>();
        locationAdapter = new LocationAdapter(locationList, this, dbHelper);

        int count = 0;

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        nearbyContainer.setLayoutManager(layoutManager);
        nearbyContainer.setAdapter(locationAdapter);

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {"Image_Id", "Tags"};

        Cursor cursor = db.query("IMAGE_TAG", projection, null, null, null, null, "Image_Id DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int imageid = cursor.getInt(cursor.getColumnIndexOrThrow("Image_Id"));
                String tags = cursor.getString(cursor.getColumnIndexOrThrow("Tags"));

                Location location = new Location(imageid, tags);

                String[] locationParts = tags.split(", ");
                double targetLat = Double.parseDouble(locationParts[0]); // Replace with actual latitude to check
                double targetLon = Double.parseDouble(locationParts[1]);  // Replace with actual longitude to check
                double radiusKm = 5.0;

                if (isWithinRadius(selectedImg_lat, selectedImg_lon, targetLat, targetLon, radiusKm)) {
                    if (imageid != imgId) {
                        //Toast.makeText(this, "The location is within 5 km radius.", Toast.LENGTH_SHORT).show();
                        locationList.add(location);
                        count++;
                    }
                } else {
                    //Toast.makeText(this, "The location is not within 5 km radius.", Toast.LENGTH_SHORT).show();
                }

            } while (cursor.moveToNext());
            //detectTxt.setVisibility(View.GONE);
            cursor.close();
            locationAdapter.notifyDataSetChanged();
        } else {
            //detectTxt.setVisibility(View.VISIBLE);
        }

        if (count == 0) {
            nearbydetectTxt.setVisibility(View.VISIBLE);
        }

        db.close();
    }
}