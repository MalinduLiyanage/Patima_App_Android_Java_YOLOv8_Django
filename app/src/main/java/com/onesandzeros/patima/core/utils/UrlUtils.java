package com.onesandzeros.patima.core.utils;

import com.onesandzeros.patima.core.config.Config;

public class UrlUtils {
    public static String getFullUrl(String relative_path) {
        return Config.BASE_URL + relative_path;
    }
}
