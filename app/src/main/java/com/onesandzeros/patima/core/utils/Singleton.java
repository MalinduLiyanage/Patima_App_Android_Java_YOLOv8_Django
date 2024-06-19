package com.onesandzeros.patima.core.utils;

import android.app.Application;

public class Singleton extends Application {
    private static Singleton instance;
    private boolean isFunctionRun = false;

    public static Singleton getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public boolean isFunctionRun() {
        return isFunctionRun;
    }

    public void setFunctionRun(boolean isFunctionRun) {
        this.isFunctionRun = isFunctionRun;
    }
}
