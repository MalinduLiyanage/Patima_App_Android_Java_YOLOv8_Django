package com.onesandzeros.patima.feedback.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface FeedbackApiService {
    @GET("/api/feedback/user/predicted/all")
    Call<FeedbackResponse> retrieveFeedbacksByPredId(@Query("pred_id") int predId);
}
