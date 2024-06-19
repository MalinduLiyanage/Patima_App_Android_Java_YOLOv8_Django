package com.onesandzeros.patima.authentication.network;


import com.onesandzeros.patima.authentication.models.ForgotPasswordRequest;
import com.onesandzeros.patima.authentication.models.LoginRequest;
import com.onesandzeros.patima.authentication.models.LoginResponse;
import com.onesandzeros.patima.authentication.models.AuthCommonResponse;
import com.onesandzeros.patima.user.model.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthenticationApiService {
    @POST("/api/account/auth/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @POST("/api/account/auth/register")
    Call<AuthCommonResponse> register(@Body User user);

    @POST("/api/account/auth/forgot-password")
    Call<AuthCommonResponse> forgotPassword(@Body ForgotPasswordRequest forgotPasswordRequest);

}