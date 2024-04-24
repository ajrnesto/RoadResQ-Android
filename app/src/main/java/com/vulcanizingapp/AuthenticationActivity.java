package com.vulcanizingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.vulcanizingapp.Utils.Utils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class AuthenticationActivity extends AppCompatActivity {

    FirebaseFirestore DB;
    FirebaseAuth AUTH;
    FirebaseUser USER;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }

    // authentication views
    ConstraintLayout clLogin;
    TextInputEditText etLoginEmail, etLoginPassword;
    MaterialButton btnLogin, btnGotoSignup;
    
    // registration views
    ConstraintLayout clSignup;
    TextInputEditText etSignupFirstName, etSignupLastName, etSignupMobile, etSignupEmail, etSignupPassword;
    MaterialButton btnSignup, btnGotoLogin, btnForgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        initializeFirebase();
        checkPreviousLoggedSession();
        initializeViews();
        handleUserInteractions();
    }

    private void checkPreviousLoggedSession() {
        if (USER != null) {
            startActivity(new Intent(AuthenticationActivity.this, MainActivity.class));
            finish();
        }
    }

    private void handleUserInteractions() {
        btnForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AuthenticationActivity.this, ForgotPasswordActivity.class));
            }
        });

        btnSignup.setOnClickListener(view -> {
            Utils.hideKeyboard(this);
            validateRegistrationForm();
        });

        btnLogin.setOnClickListener(view -> {
            Utils.hideKeyboard(this);
            validateAuthenticationForm();
        });

        btnGotoSignup.setOnClickListener(view -> {
            clLogin.setVisibility(View.GONE);
            clSignup.setVisibility(View.VISIBLE);
        });

        btnGotoLogin.setOnClickListener(view -> {
            clLogin.setVisibility(View.VISIBLE);
            clSignup.setVisibility(View.GONE);
        });
    }

    private void validateAuthenticationForm() {
        if (etLoginEmail.getText().toString().isEmpty() ||
            etLoginPassword.getText().toString().isEmpty())
        {
            Toast.makeText(this, "Please fill out all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);

        String email = etLoginEmail.getText().toString();
        String password = etLoginPassword.getText().toString();

        AUTH.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(AuthenticationActivity.this, "Signed in as "+email, Toast.LENGTH_SHORT).show();
                        if (AUTH.getCurrentUser().isEmailVerified()) {
                            startActivity(new Intent(AuthenticationActivity.this, MainActivity.class));
                        }
                        else {
                            startActivity(new Intent(AuthenticationActivity.this, UnverifiedEmailActivity.class));
                        }
                        finish();
                    }
                    else {
                        Utils.basicDialog(this, "Incorrect email or password.", "Try again");
                        btnLogin.setEnabled(true);
                    }
                });
    }

    private void validateRegistrationForm() {
        if (etSignupFirstName.getText().toString().isEmpty() ||
            etSignupLastName.getText().toString().isEmpty() ||
            etSignupMobile.getText().toString().isEmpty() ||
            etSignupEmail.getText().toString().isEmpty() ||
            etSignupPassword.getText().toString().isEmpty())
        {
            Toast.makeText(this, "Please fill out all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String firstName = etSignupFirstName.getText().toString().toUpperCase();
        String lastName = etSignupLastName.getText().toString().toUpperCase();
        String mobile = etSignupMobile.getText().toString().toUpperCase();
        String email = etSignupEmail.getText().toString();
        String password = etSignupPassword.getText().toString();

        if (password.length() < 6) {
            Utils.basicDialog(this, "Please use a password with at least 6 characters.", "Okay");
            return;
        }

        btnSignup.setEnabled(false);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("firstName", firstName);
        userInfo.put("lastName", lastName);
        userInfo.put("mobile", mobile);
        userInfo.put("email", email);
        userInfo.put("password", password);
        userInfo.put("userType", 0);

        AUTH.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userInfo.put("uid", AUTH.getUid());
                        userInfo.put("uidReadable",  lastName.toLowerCase() + Utils.randomNumberBetween(1000, 9999));
                        DB.collection("users").document(AUTH.getUid())
                                .set(userInfo)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            startActivity(new Intent(AuthenticationActivity.this, UnverifiedEmailActivity.class));
                                            Utils.Cache.setInt(AuthenticationActivity.this, "user_type", 0);
                                            finish();
                                            btnSignup.setEnabled(true);
                                        }
                                        else {
                                            Toast.makeText(AuthenticationActivity.this, "Registration error: "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            btnSignup.setEnabled(true);
                                        }
                                    }
                                });
                    }
                    else {
                        Utils.simpleDialog(this, "Registration Failed",""+task.getException().getMessage(), "Try again");
                        btnSignup.setEnabled(true);
                    }
                });
    }

    private void initializeViews() {
        btnForgotPassword = findViewById(R.id.btnForgotPassword);
        clLogin = findViewById(R.id.clLogin);
        clSignup = findViewById(R.id.clSignup);
        etLoginEmail = findViewById(R.id.etLoginEmail);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGotoSignup = findViewById(R.id.btnGotoSignup);
        etSignupFirstName = findViewById(R.id.etSignupFirstName);
        etSignupLastName = findViewById(R.id.etSignupLastName);
        etSignupMobile = findViewById(R.id.etSignupMobile);
        etSignupEmail = findViewById(R.id.etSignupEmail);
        etSignupPassword = findViewById(R.id.etSignupPassword);
        btnSignup = findViewById(R.id.btnSignup);
        btnGotoLogin = findViewById(R.id.btnGotoLogin);
    }
}