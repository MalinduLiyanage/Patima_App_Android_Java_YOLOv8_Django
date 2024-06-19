package com.onesandzeros.patima.user.network;

import com.onesandzeros.patima.user.model.AccountResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface AccountApiService {
    @GET("/api/account/retrieve")
    Call<AccountResponse> retrieveAccount();
}
