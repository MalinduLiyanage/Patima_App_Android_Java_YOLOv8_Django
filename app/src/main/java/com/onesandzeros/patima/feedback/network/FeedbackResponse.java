package com.onesandzeros.patima.feedback.network;

import com.onesandzeros.patima.feedback.model.Feedback;

import java.util.List;

public class FeedbackResponse {
    private String status;
    private String message;
    private List<Feedback> feedbacks;

    public List<Feedback> getFeedbacks() {
        return feedbacks;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
