package com.vulcanizingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ForgotPasswordActivity extends AppCompatActivity {

    FirebaseFirestore DB;
    FirebaseAuth AUTH;
    FirebaseUser USER;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }

    TextView tvResetPasswordEmailSent;
    TextInputEditText etEmail;
    MaterialButton btnSendPasswordResetEmail, btnGotoLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        initializeFirebase();
        initializeViews();
        handleUserInteraction();
    }

    private void handleUserInteraction() {
        btnSendPasswordResetEmail.setOnClickListener(view -> {
            btnSendPasswordResetEmail.setEnabled(false);

            AUTH.sendPasswordResetEmail(etEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        tvResetPasswordEmailSent.setVisibility(View.VISIBLE);
                        btnGotoLogin.setVisibility(View.VISIBLE);
                        etEmail.setVisibility(View.GONE);
                        btnSendPasswordResetEmail.setVisibility(View.GONE);
                    }
                    else {
                        btnSendPasswordResetEmail.setEnabled(true);
                    }
                }
            });
        });

        btnGotoLogin.setOnClickListener(view -> {
            startActivity(new Intent(ForgotPasswordActivity.this, AuthenticationActivity.class));
            finish();
        });
    }

    private void initializeViews() {
        tvResetPasswordEmailSent = findViewById(R.id.tvResetPasswordEmailSent);
        etEmail = findViewById(R.id.etEmail);
        btnSendPasswordResetEmail = findViewById(R.id.btnSendPasswordResetEmail);
        btnGotoLogin = findViewById(R.id.btnGotoLogin);
    }
}