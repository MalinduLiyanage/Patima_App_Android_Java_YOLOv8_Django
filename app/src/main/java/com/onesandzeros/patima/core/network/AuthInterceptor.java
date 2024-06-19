package com.onesandzeros.patima.core.network;

import android.content.Context;

import com.onesandzeros.patima.core.utils.TokenManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private Context context;

    public AuthInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        String authToken = TokenManager.getToken(context);
        Request originalRequest = chain.request();

        Request.Builder builder = originalRequest.newBuilder();

        String url = originalRequest.url().toString();
        if (url.contains("login") || url.contains("register") || url.contains("user/send")) {
            return chain.proceed(originalRequest);
        }

        if (authToken != null) {
            builder.header("Authorization", "Bearer " + authToken);
        }

        Request modifiedRequest = builder.build();
        return chain.proceed(modifiedRequest);
    }
}
