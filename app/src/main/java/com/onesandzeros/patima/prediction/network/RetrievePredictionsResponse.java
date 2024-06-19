package com.onesandzeros.patima.prediction.network;

import com.onesandzeros.patima.prediction.model.Image;

public class RetrievePredictionsResponse {
    private String status;
    private Image[] predictions;

    public Image[] getPredictions() {
        return predictions;
    }
}
