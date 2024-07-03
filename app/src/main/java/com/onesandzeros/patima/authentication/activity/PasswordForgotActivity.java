package com.onesandzeros.patima.authentication.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.onesandzeros.patima.MainActivity;
import com.onesandzeros.patima.R;
import com.onesandzeros.patima.authentication.network.AuthCommonResponse;
import com.onesandzeros.patima.authentication.network.AuthenticationApiService;
import com.onesandzeros.patima.authentication.network.ForgotPasswordRequest;
import com.onesandzeros.patima.core.network.ApiClient;
import com.onesandzeros.patima.shared.LoadingDialog;

import retrofit2.Call;

public class PasswordForgotActivity extends AppCompatActivity {
    private Button btnSubmit;
    private EditText email_;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_password_forgot);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadingDialog = new LoadingDialog(this);

        email_ = findViewById(R.id.email_text);
        btnSubmit = findViewById(R.id.submit_btn);

        btnSubmit.setOnClickListener(v -> {
            if (checkFields()) {
                // Call API
                forgotPassword();
            }

        });
    }

    private boolean checkFields() {
        String email = email_.getText().toString();

        email_.setError(null);

        boolean isValid = true;
        if (TextUtils.isEmpty(email)) {
            email_.setError("Email is required");
            isValid = false;
        } else {
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                email_.setError("Invalid Email");
                isValid = false;
            }
        }
        return isValid;
    }

    private void forgotPassword() {
        // Call API
        AuthenticationApiService authenticationApiService = ApiClient.getClient(this).create(AuthenticationApiService.class);
        ForgotPasswordRequest forgotPasswordRequest = new ForgotPasswordRequest(email_.getText().toString());
        Call<AuthCommonResponse> call = authenticationApiService.forgotPassword(forgotPasswordRequest);
        loadingDialog.show();
        call.enqueue(new retrofit2.Callback<AuthCommonResponse>() {
            @Override
            public void onResponse(Call<AuthCommonResponse> call, retrofit2.Response<AuthCommonResponse> response) {
                loadingDialog.dismiss();
                if (response.isSuccessful()) {
                    AuthCommonResponse authCommonResponse = response.body();
                    if (authCommonResponse != null) {
                        if (authCommonResponse.getStatus().equals("success")) {
                            // Success
                            Toast.makeText(PasswordForgotActivity.this, authCommonResponse.getMessage(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(PasswordForgotActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                } else {
                    // Error
                    try {
                        AuthCommonResponse authCommonResponse = new Gson().fromJson(response.errorBody().charStream(), AuthCommonResponse.class);
                        if (authCommonResponse != null) {
                            Toast.makeText(PasswordForgotActivity.this, authCommonResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<AuthCommonResponse> call, Throwable t) {
                // Error
                loadingDialog.dismiss();
                Toast.makeText(PasswordForgotActivity.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}