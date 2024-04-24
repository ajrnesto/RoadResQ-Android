package com.vulcanizingapp.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.vulcanizingapp.R;
import com.vulcanizingapp.Utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Objects;

public class WaitRescueFragment extends Fragment {

    FirebaseFirestore DB;
    FirebaseAuth AUTH;
    FirebaseUser USER;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }
    View view;
    long timestamp;
    String description, status, brand, model;
    TextView tvStatusText, tvTimestamp, tvDescription, tvStatus, tvBrand, tvModel;
    MaterialButton btnCancel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_wait_rescue, container, false);

        initializeFirebase();
        initializeViews();
        loadRescueDetails();

        btnCancel.setOnClickListener(view -> DB.collection("rescue").document(AUTH.getCurrentUser().getUid())
                .delete());

        return view;
    }

    private void loadRescueDetails() {
        DB.collection("rescue").document(AUTH.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot rescue, @Nullable FirebaseFirestoreException error) {

                        if (Objects.equals(rescue.getString("status"), "PENDING")) {
                            btnCancel.setVisibility(View.VISIBLE);
                            tvStatusText.setText("Waiting for Rescue");
                        }
                        else {
                            if (Objects.equals(rescue.getString("status"), "IN SERVICE")) {
                                tvStatusText.setText("Our mechanic will now start servicing your vehicle");
                            }
                            else if (Objects.equals(rescue.getString("status"), "COMPLETED")) {
                                tvStatusText.setText("Our mechanic has finished servicing your vehicle");
                            }
                            btnCancel.setVisibility(View.GONE);
                        }

                        if (!rescue.exists()) {
                            requireActivity().onBackPressed();
                        }
                        else {
                            long timestamp = rescue.getLong("timestamp");
                            String description = rescue.getString("description");
                            String status = rescue.getString("status");
                            String brand = rescue.getString("brand");
                            String model = rescue.getString("model");

                            SimpleDateFormat sdfTimestamp = new SimpleDateFormat("MMMM dd, yyyy, hh:mm aa");
                            tvTimestamp.setText(sdfTimestamp.format(timestamp));
                            tvDescription.setText(description);
                            tvStatus.setText(Utils.capitalizeEachWord(status));
                            tvBrand.setText(brand);
                            tvModel.setText(model);
                        }
                    }
                });
    }

    private void initializeViews() {
        tvTimestamp = view.findViewById(R.id.tvTimestamp);
        tvStatusText = view.findViewById(R.id.tvStatusText);
        tvDescription = view.findViewById(R.id.tvDescription);
        tvStatus = view.findViewById(R.id.tvStatus);
        tvBrand = view.findViewById(R.id.tvBrand);
        tvModel = view.findViewById(R.id.tvModel);
        btnCancel = view.findViewById(R.id.btnCancel);
    }
}