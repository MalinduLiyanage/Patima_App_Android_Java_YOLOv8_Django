package com.onesandzeros.patima.prediction.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.onesandzeros.patima.R;
import com.onesandzeros.patima.core.network.ApiClient;
import com.onesandzeros.patima.prediction.model.NewPredictResponse;
import com.onesandzeros.patima.prediction.network.PredictionApiService;
import com.onesandzeros.patima.prediction.utils.AcquisitionManager;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProcessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_process);

        int img_Id = 0;

        Intent intent = getIntent();
        if (intent != null) {
            img_Id = intent.getIntExtra("imgId", 0); // 0 is the default value if INT_VALUE is not found
            //Toast.makeText(this, String.valueOf(img_Id), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "NULL", Toast.LENGTH_SHORT).show();
        }

        String captured_path = AcquisitionManager.getAcquisitionPath(this);
        File file = new File(captured_path);
        RequestBody reqFile = RequestBody.create(file, MediaType.parse("image/*"));
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), reqFile);

        PredictionApiService predictionApiService = ApiClient.getClient(this).create(PredictionApiService.class);
        Call<NewPredictResponse> call = predictionApiService.newPrediction(body);
        call.enqueue(new Callback<NewPredictResponse>() {
            @Override
            public void onResponse(Call<NewPredictResponse> call, Response<NewPredictResponse> response) {
                if (response.isSuccessful()) {
                    NewPredictResponse newPredictResponse = response.body();
                    if (newPredictResponse != null) {
                        String input_image_path = newPredictResponse.getPrediction().getInputImagePath();
                        String predicted_image_path = newPredictResponse.getPrediction().getPredictedImagePath();
                        int prediction_id = newPredictResponse.getPrediction().getImageId();
                        Intent intent = new Intent(ProcessActivity.this, ViewComparisonActivity.class);
                        intent.putExtra("input_image_path", input_image_path);
                        intent.putExtra("predicted_image_path", predicted_image_path);
                        intent.putExtra("prediction_id", prediction_id);
                        startActivity(intent);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        finish(); // Finish the activity
                    }
                }
            }

            @Override
            public void onFailure(Call<NewPredictResponse> call, Throwable t) {
                Toast.makeText(ProcessActivity.this, "Failed to process image", Toast.LENGTH_SHORT).show();
                Log.e("Error", t.getMessage());
            }
        });

    }
}