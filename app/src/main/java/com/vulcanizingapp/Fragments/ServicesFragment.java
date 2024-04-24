package com.vulcanizingapp.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
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
import com.vulcanizingapp.Adapters.ServiceAdapter;
import com.vulcanizingapp.Objects.Service;
import com.vulcanizingapp.R;

import java.util.ArrayList;
import java.util.Arrays;

public class ServicesFragment extends Fragment implements ServiceAdapter.OnServicesListener {

    FirebaseFirestore DB;
    FirebaseAuth AUTH;
    FirebaseUser USER;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }
    View view;

    ArrayList<Service> arrServices;
    ServiceAdapter serviceAdapter;
    ServiceAdapter.OnServicesListener onServicesListener = this;

    MaterialButton btnAppointments;
    RecyclerView rvServices;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_services, container, false);

        initializeFirebase();
        initializeViews();
        loadRecyclerView();
        handleUserInteractions();

        return view;
    }

    private void initializeViews() {
        rvServices = view.findViewById(R.id.rvServices);
        btnAppointments = view.findViewById(R.id.btnAppointments);
    }

    private void loadRecyclerView() {
        arrServices = new ArrayList<>();
        rvServices.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        rvServices.setLayoutManager(linearLayoutManager);

        CollectionReference refServices = DB.collection("services");

        Query qryServices = refServices.whereEqualTo("status", true);
        qryServices.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                arrServices.clear();

                if (queryDocumentSnapshots == null) {
                    return;
                }

                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                    Service service = documentSnapshot.toObject(Service.class);

                    arrServices.add(service);
                    serviceAdapter.notifyDataSetChanged();
                }

                if (arrServices.isEmpty()) {
                    rvServices.setVisibility(View.GONE);
                }
                else {
                    rvServices.setVisibility(View.VISIBLE);
                }
            }
        });

        serviceAdapter = new ServiceAdapter(getContext(), arrServices, onServicesListener);
        rvServices.setAdapter(serviceAdapter);
        serviceAdapter.notifyDataSetChanged();
    }

    private void handleUserInteractions() {
        btnAppointments.setOnClickListener(view -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment appointmentsFragment = new AppointmentsFragment();
            fragmentTransaction.replace(R.id.fragmentHolder, appointmentsFragment, "APPOINTMENTS_FRAGMENT");
            fragmentTransaction.addToBackStack("APPOINTMENTS_FRAGMENT");
            fragmentTransaction.commit();
        });
    }

    @Override
    public void onServicesClick(int position) {
        Service service = arrServices.get(position);
        Bundle args = new Bundle();
        args.putString("selected_service", service.getServiceName());
        args.putDouble("selected_service_fee", service.getPrice());

        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment formAppointmentFragment = new FormAppointmentFragment();
        formAppointmentFragment.setArguments(args);
        fragmentTransaction.replace(R.id.fragmentHolder, formAppointmentFragment, "FORM_APPOINTMENTS_FRAGMENT");
        fragmentTransaction.addToBackStack("FORM_APPOINTMENTS_FRAGMENT");
        fragmentTransaction.commit();
    }
}