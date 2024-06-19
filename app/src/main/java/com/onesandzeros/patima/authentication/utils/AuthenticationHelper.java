package com.onesandzeros.patima.authentication.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.onesandzeros.patima.core.utils.TokenManager;
import com.onesandzeros.patima.user.utils.ProfileManager;

public class AuthenticationHelper {
    public static Boolean logOut(Context context) {
        TokenManager.clearToken(context);
        ProfileManager.clearProfile(context);
        return true;
    }
}
