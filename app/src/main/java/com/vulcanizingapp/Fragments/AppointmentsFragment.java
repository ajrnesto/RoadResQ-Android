package com.vulcanizingapp.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vulcanizingapp.Adapters.AppointmentAdapter;
import com.vulcanizingapp.Objects.Appointment;
import com.vulcanizingapp.R;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;

public class AppointmentsFragment extends Fragment implements AppointmentAdapter.OnAppointmentsListener {

    FirebaseFirestore DB;
    FirebaseAuth AUTH;
    FirebaseUser USER;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }
    View view;

    ArrayList<Appointment> arrAppointments;
    AppointmentAdapter appointmentAdapter;
    AppointmentAdapter.OnAppointmentsListener onAppointmentsListener = this;
    TabLayout tlAppointmentStatus;

    RecyclerView rvAppointments;
    ExtendedFloatingActionButton btnBookAnAppointment;
    TextView tvEmpty;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_appointments, container, false);

        initializeFirebase();
        initializeViews();
        loadRecyclerView(tlAppointmentStatus.getSelectedTabPosition());
        handleUserInteractions();

        return view;
    }

    private void initializeViews() {
        btnBookAnAppointment = view.findViewById(R.id.btnBookAnAppointment);
        rvAppointments = view.findViewById(R.id.rvAppointments);
        tlAppointmentStatus = view.findViewById(R.id.tlAppointmentStatus);
        tvEmpty = view.findViewById(R.id.tvEmpty);
    }

    private void loadRecyclerView(int tabIndex) {
        arrAppointments = new ArrayList<>();
        rvAppointments.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        rvAppointments.setLayoutManager(linearLayoutManager);

        CollectionReference refAppointments = DB.collection("appointments");
        Query qryAppointments = null;

        if (tabIndex == 0) {
            qryAppointments = refAppointments.whereEqualTo("userUid", AUTH.getUid()).whereEqualTo("status", "PENDING");
        }
        else if (tabIndex == 1) {
            qryAppointments = refAppointments.whereEqualTo("userUid", AUTH.getUid()).whereEqualTo("status", "IN SERVICE");
        }
        else if (tabIndex == 2) {
            qryAppointments = refAppointments.whereEqualTo("userUid", AUTH.getUid()).whereIn("status", Arrays.asList("COMPLETED", "DECLINED"));
        }

        qryAppointments.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                arrAppointments.clear();

                if (queryDocumentSnapshots == null) {
                    return;
                }

                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                    Appointment appointment = documentSnapshot.toObject(Appointment.class);

                    arrAppointments.add(appointment);
                    appointmentAdapter.notifyDataSetChanged();
                }

                if (arrAppointments.isEmpty()) {
                    rvAppointments.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                    if (tlAppointmentStatus.getSelectedTabPosition() == 0) {
                        tvEmpty.setText("No pending appointments");
                    }
                    else if (tlAppointmentStatus.getSelectedTabPosition() == 1) {
                        tvEmpty.setText("No appointments in service");
                    }
                    else if (tlAppointmentStatus.getSelectedTabPosition() == 2) {
                        tvEmpty.setText("No appointments in completed");
                    }
                }
                else {
                    rvAppointments.setVisibility(View.VISIBLE);
                    tvEmpty.setVisibility(View.GONE);
                }
            }
        });

        appointmentAdapter = new AppointmentAdapter(getContext(), arrAppointments, onAppointmentsListener);
        rvAppointments.setAdapter(appointmentAdapter);
        appointmentAdapter.notifyDataSetChanged();
    }

    private void handleUserInteractions() {
        tlAppointmentStatus.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                loadRecyclerView(tlAppointmentStatus.getSelectedTabPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        btnBookAnAppointment.setOnClickListener(view -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment formAppointmentFragment = new FormAppointmentFragment();
            fragmentTransaction.replace(R.id.fragmentHolder, formAppointmentFragment, "APPOINTMENT_FORM_FRAGMENT");
            fragmentTransaction.addToBackStack("APPOINTMENT_FORM_FRAGMENT");
            fragmentTransaction.commit();
        });
    }

    @Override
    public void onAppointmentsClick(int position) {

    }
}