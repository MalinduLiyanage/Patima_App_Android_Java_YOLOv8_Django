package com.onesandzeros.patima.core.network;

import android.content.Context;
import android.content.Intent;

import com.onesandzeros.patima.MainActivity;
import com.onesandzeros.patima.authentication.utils.AuthenticationHelper;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.Response;

public class UnauthorizedInterceptor implements Interceptor {
    private final Context context;
    private final List<String> excludedUrls = Arrays.asList("login", "register", "user/send");

    public UnauthorizedInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        String requestUrl = chain.request().url().toString();
        Response response = chain.proceed(chain.request());

        boolean isUrlExcluded = excludedUrls.stream().anyMatch(requestUrl::contains);

        if (!isUrlExcluded && response.code() == 401) {
            // Handle unauthorized request
            Boolean isLoggedOut = AuthenticationHelper.logOut(context);
            if (isLoggedOut) {
                Intent intent = new Intent(context, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);
            }
        }
        return response;
    }
}
