package com.onesandzeros.patima.core.config;

import com.onesandzeros.patima.BuildConfig;

public class Config {
    //    public static final String BASE_URL = "http://10.0.2.2:8000/";
    public static final String BASE_URL = "http://140.238.225.128/";
    public static final String MODEL_PATH = "model.tflite";
    public static final String LABELS_PATH = "labels.txt";

    //Segmentation Model Switch Properties
    public static final String SEG_MODEL_URL = "https://drive.usercontent.google.com/download?id=1s6MTT-NZOFhhkaUuRb017oiQ1fec7_r5&export=download&authuser=0&confirm=t&uuid=676ec4f8-df15-481c-9a0f-cd849871ce51&at=APvzH3qlMXkuM3NcslfhumPqjuCe%3A1734961941776";
    public static final String SEG_MODEL_FILE_NAME = "unet_model.tflite";

    //If Cloud
    public static final String CLOUD_TEXT = "CLOUD BASED SEGMENTATION";
    public static final String CLOUD_SUB_TEXT = "By default, the Application uses the Cloud based Segmentation method. If you prefer, you may use the Local Segmentation method as well. Enabling this Switch will download the Model into your device and it will take around 450 MB to download.";

    //If Local
    public static final String LOCAL_TEXT = "LOCAL BASED SEGMENTATION";
    public static final String LOCAL_SUB_TEXT = "Now App is using the downloaded Segmentation Model. By switching this Switch off, you can continue to Cloud based Segmentation.";
}
