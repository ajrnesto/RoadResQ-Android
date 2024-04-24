package com.vulcanizingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class UnverifiedEmailActivity extends AppCompatActivity {

    FirebaseFirestore DB;
    FirebaseAuth AUTH;
    FirebaseUser USER;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }

    MaterialButton btnBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unverified_email);

        initializeFirebase();
        initializeViews();
        handleUserInteraction();

        USER.sendEmailVerification();
    }

    @Override
    protected void onResume() {
        super.onResume();
        USER.reload().addOnCompleteListener(task -> {
            USER = AUTH.getCurrentUser();
            if (USER.isEmailVerified()) {
                startActivity(new Intent(UnverifiedEmailActivity.this, MainActivity.class));
                finish();
            }
        });
    }

    private void handleUserInteraction() {
        btnBackToLogin.setOnClickListener(view -> {
            AUTH.signOut();
            startActivity(new Intent(UnverifiedEmailActivity.this, AuthenticationActivity.class));
            finish();
        });
    }

    private void initializeViews() {
        btnBackToLogin = findViewById(R.id.btnGotoLogin);
    }
}