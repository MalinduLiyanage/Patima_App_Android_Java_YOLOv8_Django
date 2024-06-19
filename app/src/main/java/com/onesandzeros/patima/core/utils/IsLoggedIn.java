package com.onesandzeros.patima.core.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

public class IsLoggedIn {
    public static boolean isLoggedIn(Context context) {
        String accessToken = null;
        SharedPreferences sharedPreferences = context.getSharedPreferences("patima", context.MODE_PRIVATE);
        if (sharedPreferences.contains("access")) {
            accessToken = sharedPreferences.getString("access", "");
        }
        if (accessToken != null) {
            String[] splitToken = accessToken.split("\\.");
            if (splitToken.length >= 3) {
                String payload = new String(android.util.Base64.decode(splitToken[1], Base64.DEFAULT));
                try {
                    JSONObject jsonPayload = new JSONObject(payload);
                    long exp = jsonPayload.getLong("exp");
                    if (exp < System.currentTimeMillis() / 1000) {
                        return false;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else {
            return false;
        }
        return true;
    }
}
