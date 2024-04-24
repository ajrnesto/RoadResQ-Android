 package com.vulcanizingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vulcanizingapp.Utils.Utils;

 public class SplashActivity extends AppCompatActivity {

     FirebaseFirestore DB;
     FirebaseAuth AUTH;
     FirebaseUser USER;

     private void initializeFirebase() {
         DB = FirebaseFirestore.getInstance();
         AUTH = FirebaseAuth.getInstance();
         USER = AUTH.getCurrentUser();
     }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initializeFirebase();

        Utils.Cache.setBoolean(getApplicationContext(), "appointment_items_selection_mode", false);
        Utils.Cache.setBoolean(getApplicationContext(), "mode", false);

        Handler handler = new Handler();
        handler.postDelayed(() -> {
            if (USER != null) {
                if (USER.isEmailVerified()) {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                }
                else {
                    startActivity(new Intent(SplashActivity.this, UnverifiedEmailActivity.class));
                }
            }
            else {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            }
            finish();
        }, 2000);
    }
}