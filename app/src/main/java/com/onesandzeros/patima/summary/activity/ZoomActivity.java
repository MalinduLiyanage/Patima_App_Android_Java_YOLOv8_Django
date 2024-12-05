package com.onesandzeros.patima.summary.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.onesandzeros.patima.R;
import com.onesandzeros.patima.core.utils.UrlUtils;
import com.onesandzeros.patima.summary.model.ViewPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class ZoomActivity extends AppCompatActivity {

    String input_image_path, predicted_image_path;
    ViewPager2 viewPager;
    List<Uri> imageUris;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoom);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        viewPager = findViewById(R.id.view_pager);

        Intent intent = getIntent();
        if (intent != null) {
            input_image_path = intent.getStringExtra("input_image_path");
            predicted_image_path = intent.getStringExtra("predicted_image_path");

            if (input_image_path != null && !input_image_path.isEmpty() &&
                    predicted_image_path != null && !predicted_image_path.isEmpty()) {

                imageUris = new ArrayList<>();
                imageUris.add(Uri.parse(UrlUtils.getFullUrl(input_image_path)));
                imageUris.add(Uri.parse(UrlUtils.getFullUrl(predicted_image_path)));
                ViewPagerAdapter adapter = new ViewPagerAdapter(this, imageUris);
                viewPager.setAdapter(adapter);

            } else {
                Toast.makeText(this, "Invalid image paths", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "NULL Intent", Toast.LENGTH_SHORT).show();
        }
    }
}
