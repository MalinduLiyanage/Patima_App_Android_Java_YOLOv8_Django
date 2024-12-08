package com.onesandzeros.patima.summary.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.onesandzeros.patima.R;
import com.onesandzeros.patima.core.network.ApiClient;
import com.onesandzeros.patima.core.utils.UrlUtils;
import com.onesandzeros.patima.feedback.activity.FeedbackActivity;
import com.onesandzeros.patima.feedback.adapter.FeedbackAdapter;
import com.onesandzeros.patima.feedback.model.Feedback;
import com.onesandzeros.patima.feedback.network.FeedbackApiService;
import com.onesandzeros.patima.feedback.network.FeedbackResponse;
import com.onesandzeros.patima.shared.LoadingDialog;
import com.onesandzeros.patima.summary.adapter.LocationAdapter;
import com.onesandzeros.patima.summary.model.NearbyPredictions;
import com.onesandzeros.patima.summary.network.NearbyPredictionsResponse;
import com.onesandzeros.patima.summary.network.SummaryApiService;
import com.onesandzeros.patima.user.utils.ProfileManager;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SummaryActivity extends AppCompatActivity {
    String basePath = null, detectionPath = null, location = null;
    int imgId = 0;
    FloatingActionButton feedbackBtn, zoomBtn;
    TextView feedbackTextShow, detectTxt, nearbydetectTxt, locationTxt, locationName;
    RecyclerView feedbackContainer, nearbyContainer;
    ImageButton backBtn;
    //    private ImageView baseImg, processedImg;
    private SimpleDraweeView baseImg, processedImg;
    private List<Feedback> feedbackList;
    private FeedbackAdapter feedbackAdapter;
    private List<NearbyPredictions> locationList;
    private LocationAdapter locationAdapter;

    private LoadingDialog loadingDialog;
    ImageView feedbackexpanderBtn;
    boolean areFeedbacksexpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        loadingDialog = new LoadingDialog(this);

        imgId = getIntent().getIntExtra("imgId", 0);
        basePath = getIntent().getStringExtra("base_path");
        detectionPath = getIntent().getStringExtra("detection_path");
        location = getIntent().getStringExtra("location");

        baseImg = findViewById(R.id.base_image);
        processedImg = findViewById(R.id.processed_image);
        feedbackBtn = findViewById(R.id.btn_feedback);
        zoomBtn = findViewById(R.id.btn_zoom);
        feedbackTextShow = findViewById(R.id.feedbackshow);
        feedbackContainer = findViewById(R.id.feedback_container);
        nearbyContainer = findViewById(R.id.nearby_container);
        detectTxt = findViewById(R.id.detect_txt);
        nearbydetectTxt = findViewById(R.id.nearby_detect_txt);
        backBtn = findViewById(R.id.return_button);
        locationTxt = findViewById(R.id.loc_text);
        locationName = findViewById(R.id.loc_name);
        feedbackexpanderBtn = findViewById(R.id.btn_feedback_expand);

        String userTypest = ProfileManager.getProfileRole(this);

        if (userTypest.equals("General Public")) {
            feedbackBtn.setVisibility(View.INVISIBLE);
            feedbackTextShow.setVisibility(View.GONE);
            feedbackContainer.setVisibility(View.GONE);
            feedbackBtn.setEnabled(false);
            feedbackexpanderBtn.setVisibility(View.GONE);
        }

        loadImages();
        nearbyImages();

        feedbackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SummaryActivity.this, FeedbackActivity.class);
                intent.putExtra("imgId", imgId);
                intent.putExtra("input_image_path", basePath);
                intent.putExtra("predicted_image_path", detectionPath);
                startActivity(intent);
            }
        });

        zoomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SummaryActivity.this, ZoomActivity.class);
                intent.putExtra("imgId", imgId);
                intent.putExtra("input_image_path", basePath);
                intent.putExtra("predicted_image_path", detectionPath);
                startActivity(intent);
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        feedbackexpanderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!areFeedbacksexpanded){
                    areFeedbacksexpanded = true;
                    feedbackexpanderBtn.setImageResource(R.drawable.ic_arrow_down);

                    ViewGroup.LayoutParams params = feedbackContainer.getLayoutParams();
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    feedbackContainer.setLayoutParams(params);

                }else{
                    areFeedbacksexpanded = false;
                    feedbackexpanderBtn.setImageResource(R.drawable.ic_arrow_right);

                    int dpValue = 100;
                    float density = getResources().getDisplayMetrics().density;
                    int heightInPixels = (int) (dpValue * density);
                    ViewGroup.LayoutParams layoutParams = feedbackContainer.getLayoutParams();
                    layoutParams.height = heightInPixels;
                    feedbackContainer.setLayoutParams(layoutParams);

                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        String userTypest = ProfileManager.getProfileRole(this);

        if (!userTypest.equals("General Public")) {
            loadPredictedFeedbacks();
        }

        nearbyImages();
        if (location != null) {
            double latitude = Double.parseDouble(location.split(",")[0]);
            double longitude = Double.parseDouble(location.split(",")[1]);
            reverseGeocode(latitude, longitude);
            locationTxt.setText(location);
        } else {
            locationTxt.setText("Image has no Location Data. Location based summary will not be available.");
            locationName.setVisibility(View.GONE);
        }

    }

    private void loadPredictedFeedbacks() {
        feedbackList = new ArrayList<>();
        feedbackAdapter = new FeedbackAdapter(feedbackList, SummaryActivity.this, true);
        feedbackContainer.setLayoutManager(new LinearLayoutManager(SummaryActivity.this));
        feedbackContainer.setAdapter(feedbackAdapter);
        FeedbackApiService feedbackApiService = ApiClient.getClient(this).create(FeedbackApiService.class);
        Call<FeedbackResponse> call = feedbackApiService.retrieveFeedbacksByPredId(imgId);
        loadingDialog.show();
        call.enqueue(new Callback<FeedbackResponse>() {
            @Override
            public void onResponse(Call<FeedbackResponse> call, Response<FeedbackResponse> response) {
                loadingDialog.dismiss();
                if (response.isSuccessful()) {
                    List<Feedback> feedbacks = response.body().getFeedbacks();
                    feedbackList.addAll(feedbacks);
                    Collections.reverse(feedbackList);
                    feedbackAdapter.notifyDataSetChanged();
                    if (feedbackList.size() == 0) {
                        detectTxt.setVisibility(View.VISIBLE);
                        feedbackContainer.setVisibility(View.GONE);
                        feedbackexpanderBtn.setVisibility(View.INVISIBLE);
                        feedbackexpanderBtn.setEnabled(false);
                    } else {
                        detectTxt.setVisibility(View.GONE);
                    }
                } else {
                    detectTxt.setVisibility(View.VISIBLE);
                    feedbackContainer.setVisibility(View.GONE);
                    feedbackexpanderBtn.setVisibility(View.INVISIBLE);
                    feedbackexpanderBtn.setEnabled(false);
                }
            }

            @Override
            public void onFailure(Call<FeedbackResponse> call, Throwable t) {
                loadingDialog.dismiss();
                detectTxt.setVisibility(View.VISIBLE);
                Toast.makeText(SummaryActivity.this, "Failed to load feedbacks", Toast.LENGTH_SHORT).show();
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
        Uri fullDetectionUri = Uri.parse(fullDetectionPath);
        Uri fullBaseUri = Uri.parse(fullBasePath);
        processedImg.setImageURI(fullDetectionUri);
        baseImg.setImageURI(fullBaseUri);

    }

    private void nearbyImages() {
        locationList = new ArrayList<>();
        locationAdapter = new LocationAdapter(locationList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        nearbyContainer.setLayoutManager(layoutManager);
        nearbyContainer.setAdapter(locationAdapter);

        SummaryApiService summaryApiService = ApiClient.getClient(this).create(SummaryApiService.class);
        Call<NearbyPredictionsResponse> call = summaryApiService.getNearbyPredictions(imgId);
        call.enqueue(new Callback<NearbyPredictionsResponse>() {
            @Override
            public void onResponse(Call<NearbyPredictionsResponse> call, Response<NearbyPredictionsResponse> response) {
                if (response.isSuccessful()) {
                    List<NearbyPredictions> predictions = response.body().getPredictions();
                    locationList.clear();
                    locationList.addAll(predictions);
                    locationAdapter.notifyDataSetChanged();
                    if (locationList.size() == 0) {
                        nearbydetectTxt.setVisibility(View.VISIBLE);
                    }
                } else {
                    nearbydetectTxt.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<NearbyPredictionsResponse> call, Throwable t) {

            }
        });
    }

    private void reverseGeocode(double latitude, double longitude) {

        final String requestString = "https://nominatim.openstreetmap.org/reverse?format=json&lat=" +
                latitude + "&lon=" + longitude + "&zoom=18&addressdetails=1";

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(requestString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();

                        JSONObject jsonResponse = new JSONObject(response.toString());
                        JSONObject address = jsonResponse.getJSONObject("address");

                        String townName = "";
                        if (address.has("town")) {
                            townName = address.getString("town") + " - " + address.getString("country");
                        } else if (address.has("village")) {
                            townName = address.getString("village") + " - " + address.getString("country");
                        } else if (address.has("state")) {
                            townName = address.getString("state") + " - " + address.getString("country");
                        }

                        final String finalTownName = townName;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!finalTownName.isEmpty()) {
                                    locationName.setText(finalTownName);
                                } else {
                                    locationName.setVisibility(View.GONE);
                                }
                            }
                        });
                    }

                    connection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}
