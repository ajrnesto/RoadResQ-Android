package com.vulcanizingapp.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vulcanizingapp.AuthenticationActivity;
import com.vulcanizingapp.MainActivity;
import com.vulcanizingapp.R;
import com.vulcanizingapp.Utils.Utils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ProfileFragment extends Fragment {

    FirebaseFirestore DB;
    FirebaseAuth AUTH;
    FirebaseUser USER;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }

    View view;
    TextView tvFullname, tvEmail;
    TextInputEditText etSignupFirstName, etSignupLastName, etSignupMobile;
    MaterialButton btnSave, btnLogOut;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_profile, container, false);

        initializeFirebase();
        initializeViews();
        loadUserInformation();
        handleUserInteraction() ;

        return view;
    }

    private void initializeViews() {
        tvFullname = view.findViewById(R.id.tvFullName);
        tvEmail = view.findViewById(R.id.tvEmail);
        btnLogOut = view.findViewById(R.id.btnLogOut);
        etSignupFirstName = view.findViewById(R.id.etSignupFirstName);
        etSignupLastName = view.findViewById(R.id.etSignupLastName);
        etSignupMobile = view.findViewById(R.id.etSignupMobile);
        btnSave = view.findViewById(R.id.btnSave);
    }

    private void loadUserInformation() {
        DB.collection("users").document(AUTH.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {
                    String firstName = snapshot.get("firstName").toString();
                    String lastName = snapshot.get("lastName").toString();
                    String mobile = snapshot.get("mobile").toString();

                    tvFullname.setText(firstName + " " + lastName);
                    tvEmail.setText(USER.getEmail());
                    etSignupFirstName.setText(firstName);
                    etSignupLastName.setText(lastName);
                    etSignupMobile.setText(mobile);
                });
    }

    private void handleUserInteraction() {
        btnSave.setOnClickListener(view -> validateUserInformationForm());

        btnLogOut.setOnClickListener(view -> signOut());
    }

    private void signOut() {
        AUTH.signOut();
        requireActivity().startActivity(new Intent(requireActivity(), AuthenticationActivity.class));
        requireActivity().finish();
    }

    private void validateUserInformationForm() {
        if (etSignupFirstName.getText().toString().isEmpty() ||
                etSignupLastName.getText().toString().isEmpty() ||
                etSignupMobile.getText().toString().isEmpty())
        {
            Toast.makeText(requireContext(), "Please fill out all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String firstName = etSignupFirstName.getText().toString().toUpperCase();
        String lastName = etSignupLastName.getText().toString().toUpperCase();
        String mobile = etSignupMobile.getText().toString().toUpperCase();

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("firstName", firstName);
        userInfo.put("lastName", lastName);
        userInfo.put("mobile", mobile);

        DB.collection("users").document(AUTH.getUid())
                .set(userInfo)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        startActivity(new Intent(requireActivity(), MainActivity.class));
                        Utils.Cache.setInt(requireActivity(), "user_type", 0);
                        requireActivity().onBackPressed();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(requireActivity(), "Registration error: "+e, Toast.LENGTH_SHORT).show();
                    }
                });
    }

}