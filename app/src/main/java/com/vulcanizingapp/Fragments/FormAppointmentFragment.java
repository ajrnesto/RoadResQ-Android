package com.vulcanizingapp.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.vulcanizingapp.Adapters.AppointmentItemAdapter;
import com.vulcanizingapp.Adapters.CartItemAdapter;
import com.vulcanizingapp.Objects.CartItem;
import com.vulcanizingapp.Objects.Service;
import com.vulcanizingapp.Objects.ShopItem;
import com.vulcanizingapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.vulcanizingapp.SelectLocationActivity;
import com.vulcanizingapp.Utils.Utils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class FormAppointmentFragment extends Fragment {

    FirebaseFirestore DB;
    FirebaseStorage STORAGE;
    FirebaseAuth AUTH;
    FirebaseUser USER;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        STORAGE = FirebaseStorage.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }

    View view;
    ConstraintLayout clLoadingBar;
    TextInputLayout tilTypeOfService, tilLocation, tilToolsNeeded;
    TextInputEditText etFirstName, etLastName, etMobile, etCustomBrand, etToolsNeeded, etSchedule, etLocation;
    TextView tvCartSubtotal, tvServiceFee, tvTotal;
    MaterialCheckBox checkIncludeCartItems;
    RecyclerView rvCart;
    AutoCompleteTextView menuIssueDescription, menuBrand, menuModel, menuTypeOfService;
    MaterialButton btnGoToShop, btnSubmit;

    // date picker items
    MaterialDatePicker.Builder<Long> Schedule;
    MaterialDatePicker<Long> dpSchedule;
    long dpScheduleSelection = 0;

    // Spinner items
    ArrayList<Service> arrServices;
    ArrayList<String> arrBrands, arrServiceNames;
    ArrayAdapter<String> adapterIssueDescription, adapterBrands, adapterModels, adapterTypeOfService;
    String[] itemsIssueDescription, itemsBrands, itemsModels;

    String selectedService;
    Double selectedServiceFee;

    // cart items
    ArrayList<CartItem> arrCartItem;
    ArrayList<ShopItem> arrProducts;
    AppointmentItemAdapter cartItemAdapter;

    @Override
    public void onResume() {
        super.onResume();
        Utils.Cache.setBoolean(requireContext(), "appointment_items_selection_mode", false);

        double latitude = Utils.Cache.getDouble(requireContext(), "selected_latitude");
        double longitude = Utils.Cache.getDouble(requireContext(), "selected_longitude");
        String addressLine = Utils.Cache.getString(requireContext(), "addressLine");

        if (latitude == 0 || longitude == 0) {
            Objects.requireNonNull(etLocation.getText()).clear();
            return;
        }

        etLocation.setText(addressLine);

        if (Utils.Cache.getBoolean(requireContext(), "sos_mode")) {
            tilToolsNeeded.setVisibility(View.VISIBLE);
            etSchedule.setVisibility(View.GONE);
            dpScheduleSelection = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy");
            etSchedule.setText(sdf.format(dpScheduleSelection).toUpperCase(Locale.ROOT));
            btnSubmit.setText("Get Rescue Now");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_form_appointment, container, false);

        initializeFirebase();
        initializeViews();
        loadRecyclerView();
        loadUserInformation();
        initializeSpinners();
        initializeDatePicker();
        handleUserInteraction();

        return view;
    }

    private void loadUserInformation() {
        DB.collection("users").document(AUTH.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot user = task.getResult();
                            etFirstName.setText(user.getString("firstName"));
                            etLastName.setText(user.getString("lastName"));
                            etMobile.setText(user.getString("mobile"));
                        }
                    }
                });
    }

    private void loadRecyclerView() {
        arrCartItem = new ArrayList<>();
        arrProducts = new ArrayList<>();
        rvCart = view.findViewById(R.id.rvCart);
        rvCart.setHasFixedSize(false);
        rvCart.setLayoutManager(new LinearLayoutManager(requireContext()));

        // empty snapshot listener
        DB.collection("carts").document(AUTH.getCurrentUser().getUid())
                .collection("items")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (value.isEmpty()) {
                            rvCart.setVisibility(View.INVISIBLE);
                        }
                        else {
                            rvCart.setVisibility(View.VISIBLE);
                        }
                    }
                });

        // cart handler
        DB.collection("carts").document(AUTH.getCurrentUser().getUid())
                .collection("items")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot value) {
                        arrCartItem.clear();
                        arrProducts.clear();

                        final int[] position = {0};
                        for (QueryDocumentSnapshot doc : value) {
                            CartItem cartItem = doc.toObject(CartItem.class);
                            arrCartItem.add(cartItem);

                            DB.collection("products").document(cartItem.getProductId())
                                    .get()
                                    .addOnSuccessListener(snapProduct -> {
                                        ShopItem product = snapProduct.toObject(ShopItem.class);

                                        arrProducts.add(product);

                                        if (arrCartItem.size() == arrProducts.size()) {
                                            cartItemAdapter.notifyDataSetChanged();


                                            if (checkIncludeCartItems.isChecked()) {
                                                calculateTotal();
                                            }
                                        }
                                        position[0]++;

                                        clLoadingBar.setVisibility(View.GONE);
                                    });
                        }

                        if (arrCartItem.isEmpty()) {
                            rvCart.setVisibility(View.INVISIBLE);
                            clLoadingBar.setVisibility(View.GONE);
                        }
                        else {
                            rvCart.setVisibility(View.VISIBLE);
                        }
                    }
                });

        cartItemAdapter = new AppointmentItemAdapter(requireContext(), arrProducts, arrCartItem);
        rvCart.setAdapter(cartItemAdapter);
    }

    private void initializeViews() {
        clLoadingBar = view.findViewById(R.id.clLoadingBar);
        etFirstName = view.findViewById(R.id.etFirstName);
        etLastName = view.findViewById(R.id.etLastName);
        etMobile = view.findViewById(R.id.etMobile);
        menuIssueDescription = view.findViewById(R.id.menuIssueDescription);
        menuBrand = view.findViewById(R.id.menuBrand);
        menuModel = view.findViewById(R.id.menuModel);
        etCustomBrand = view.findViewById(R.id.etCustomBrand);
        menuModel = view.findViewById(R.id.menuModel);
        tilToolsNeeded = view.findViewById(R.id.tilToolsNeeded);
        etSchedule = view.findViewById(R.id.etSchedule);
        etLocation = view.findViewById(R.id.etLocation);
        checkIncludeCartItems = view.findViewById(R.id.checkIncludeCartItems);
        tilTypeOfService = view.findViewById(R.id.tilTypeOfService);
        menuTypeOfService = view.findViewById(R.id.menuTypeOfService);
        etToolsNeeded = view.findViewById(R.id.etToolsNeeded);
        tilLocation = view.findViewById(R.id.tilLocation);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        btnGoToShop = view.findViewById(R.id.btnGoToShop);
        tvCartSubtotal = view.findViewById(R.id.tvCartSubtotal);
        tvServiceFee = view.findViewById(R.id.tvServiceFee);
        tvTotal = view.findViewById(R.id.tvTotal);

        selectedService = requireArguments().getString("selected_service");
        selectedServiceFee = requireArguments().getDouble("selected_service_fee");

        DecimalFormat df = new DecimalFormat("0.00");
        tvCartSubtotal.setText("Cart Subtotal: ₱0.00");

        if (Utils.Cache.getBoolean(requireContext(), "sos_mode")) {
            tvServiceFee.setText("Service Fee: To be determined");
            tvTotal.setText("Total: To be determined");
        }
        else {
            tvServiceFee.setText("Service Fee: ₱"+df.format(selectedServiceFee));
            tvTotal.setText("Total: ₱"+df.format(selectedServiceFee));
        }
    }

    private void initializeSpinners() {
        // services
        arrServices = new ArrayList<>();
        arrServiceNames = new ArrayList<>();
        CollectionReference refServices = DB.collection("services");
        refServices.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            Service service = documentSnapshot.toObject(Service.class);
                            arrServices.add(service);
                            arrServiceNames.add(service.getServiceName());
                        }
                        adapterTypeOfService = new ArrayAdapter<>(requireContext(), R.layout.list_item, arrServiceNames);
                        menuTypeOfService.setAdapter(adapterTypeOfService);
                        tilTypeOfService.setEnabled(true);

                        menuTypeOfService.setText(selectedService, false);
                    }
                });

        // brands
        itemsBrands = new String[]{"Honda", "Yamaha", "Kawasaki", "Suzuki"};
        adapterBrands = new ArrayAdapter<>(requireContext(), R.layout.list_item, itemsBrands);
        menuBrand.setAdapter(adapterBrands);
        // menuTypeOfService.setText(selectedService, false);

        // vehicle issue description
        itemsIssueDescription = new String[]{"Engine Overheating", "Battery Problems", "Starter Motor Issues", "Brake Problems", "Chain and Sprocket Wear", "Clutch Problems", "Fuel System Issues", "Electrical System Failures", "Tire Punctures and Wear", "Ignition Problems", "Oil Leaks", "Exhaust Issues", "Suspension Problems", "Headlight and Taillight Failures", "Engine Misfires", "Cooling Leaks", "Gear Shifting Difficulties", "Oil Contamination", "Vibration and Noises"};
        adapterIssueDescription = new ArrayAdapter<>(requireContext(), R.layout.list_item, itemsIssueDescription);
        menuIssueDescription.setAdapter(adapterIssueDescription);
    }

    private void initializeDatePicker() {
        Schedule = MaterialDatePicker.Builder.datePicker();
        Schedule.setTitleText("Select Schedule")
                .setSelection(System.currentTimeMillis());
        dpSchedule = Schedule.build();
        dpSchedule.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy");
            dpScheduleSelection = dpSchedule.getSelection();
            etSchedule.setText(sdf.format(dpScheduleSelection).toUpperCase(Locale.ROOT));
            etSchedule.setEnabled(true);
        });
        dpSchedule.addOnNegativeButtonClickListener(view -> {
            etSchedule.setEnabled(true);
        });
        dpSchedule.addOnCancelListener(dialogInterface -> {
            etSchedule.setEnabled(true);
        });
    }

    private void handleUserInteraction() {
        menuBrand.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    // honda models
                    itemsModels = new String[]{"Click", "Beat", "Wave", "TMX", "XRM"};
                    adapterModels = new ArrayAdapter<>(requireContext(), R.layout.list_item, itemsModels);
                    menuModel.setAdapter(adapterModels);
                }
                else if (i == 1) {
                    // yamaha models
                    itemsModels = new String[]{"Mio", "Sniper", "NMAX", "Aerox", "FZi"};
                    adapterModels = new ArrayAdapter<>(requireContext(), R.layout.list_item, itemsModels);
                    menuModel.setAdapter(adapterModels);
                }
                else if (i == 2) {
                    // kawasaki models
                    itemsModels = new String[]{"Rouser", "Ninja", "Fury", "CT100", "Dominar"};
                    adapterModels = new ArrayAdapter<>(requireContext(), R.layout.list_item, itemsModels);
                    menuModel.setAdapter(adapterModels);
                }
                else if (i == 3) {
                    // suzuki models
                    itemsModels = new String[]{"Raider", "Smash", "Skydrive", "GSX-S150", "Gixxer"};
                    adapterModels = new ArrayAdapter<>(requireContext(), R.layout.list_item, itemsModels);
                    menuModel.setAdapter(adapterModels);
                }
            }
        });

        checkIncludeCartItems.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton checkBox, boolean checked) {
                if (checked) {
                    calculateTotal();
                }
                else {
                    DecimalFormat df = new DecimalFormat("0.00");
                    tvCartSubtotal.setText("Cart Subtotal: ₱0.00");
                    tvServiceFee.setText("Service Fee: ₱"+df.format(selectedServiceFee));
                    tvTotal.setText("Total: ₱"+df.format(selectedServiceFee));
                }
            }
        });

        btnGoToShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.Cache.setBoolean(requireContext(), "appointment_items_selection_mode", true);
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                Fragment ShopFragment = new ShopFragment();
                fragmentTransaction.replace(R.id.fragmentHolder, ShopFragment, "SHOP_FRAGMENT");
                fragmentTransaction.addToBackStack("SHOP_FRAGMENT");
                fragmentTransaction.commit();
            }
        });

        etLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(requireActivity(), SelectLocationActivity.class));
            }
        });

        btnSubmit.setOnClickListener(view -> {
            validateappointmentForm();
        });

        etSchedule.setOnClickListener(view -> {
            etSchedule.setEnabled(false);
            dpSchedule.show(requireActivity().getSupportFragmentManager(), "INCIDENT_DATE_PICKER");
        });
    }

    private void validateappointmentForm() {
        btnSubmit.setEnabled(false);
        if (etFirstName.getText().toString().isEmpty() ||
                etLastName.getText().toString().isEmpty() ||
                etMobile.getText().toString().isEmpty() ||
                menuBrand.getText().toString().isEmpty() ||
                menuModel.getText().toString().isEmpty() ||
                menuTypeOfService.getText().toString().isEmpty() ||
                menuIssueDescription.getText().toString().isEmpty() ||
                etLocation.getText().toString().isEmpty() ||
                etSchedule.getText().toString().isEmpty())
        {
            Toast.makeText(requireContext(), "Please fill out all the required fields", Toast.LENGTH_SHORT).show();
            btnSubmit.setEnabled(true);
            return;
        }

        /*if (checkIncludeCartItems.isChecked()) {
            uploadAppointmentFormDataWithCart();
        }
        else {
            uploadAppointmentFormData();
        }*/
        uploadAppointmentFormDataWithCart();
    }

    private void uploadAppointmentFormDataWithCart() {
        String firstName = etFirstName.getText().toString().toUpperCase();
        String lastName = etLastName.getText().toString().toUpperCase();
        String mobile = etMobile.getText().toString().toUpperCase();
        String brand = menuBrand.getText().toString().toUpperCase();
        String model = menuModel.getText().toString().toUpperCase();
        String serviceType = menuTypeOfService.getText().toString().toUpperCase();
        String description = menuIssueDescription.getText().toString().toUpperCase();
        String additionalTools = etToolsNeeded.getText().toString().toUpperCase();
        long schedule = dpScheduleSelection;

        Map<String, Object> location = new HashMap<>();
        location.put("latitude", Utils.Cache.getDouble(requireContext(), "selected_latitude"));
        location.put("longitude", Utils.Cache.getDouble(requireContext(), "selected_longitude"));
        location.put("addressLine", Utils.Cache.getString(requireContext(), "addressLine"));

        Map<String, Object> appointment = new HashMap<>();
        appointment.put("userUid", AUTH.getUid());
        appointment.put("firstName", firstName);
        appointment.put("lastName", lastName);
        appointment.put("mobile", mobile);
        appointment.put("brand", brand);
        appointment.put("model", model);
        appointment.put("serviceType", serviceType);
        appointment.put("description", description);
        appointment.put("additionalTools", additionalTools);
        appointment.put("schedule", schedule);
        appointment.put("location", location);
        appointment.put("timestamp", System.currentTimeMillis());
        appointment.put("status", "PENDING");

        // upload all media to firebase storage
        DocumentReference refNewAppointment;
        if (Utils.Cache.getBoolean(requireContext(), "sos_mode")) {
            refNewAppointment =  DB.collection("rescue").document(AUTH.getUid());
        }
        else {
            refNewAppointment =  DB.collection("appointments").document();
        }

        appointment.put("uid", refNewAppointment.getId());
        refNewAppointment.set(appointment)
                .addOnSuccessListener(aVoid -> {

                    if (arrCartItem.size() > 0) {
                        for (CartItem cartItem : arrCartItem) {
                            DB.collection("products").document(cartItem.getProductId())
                                    .update("stock", FieldValue.increment(-cartItem.getQuantity()))
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Map<String, Object> orderItems = new HashMap<>();
                                            orderItems.put("productId", cartItem.getProductId());
                                            orderItems.put("quantity", cartItem.getQuantity());

                                            DB.collection("appointments").document(refNewAppointment.getId())
                                                    .collection("orderItems").document(cartItem.getProductId())
                                                    .set(orderItems)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            DB.collection("carts").document(AUTH.getCurrentUser().getUid())
                                                                    .collection("items").document(cartItem.getProductId())
                                                                    .delete()
                                                                    .addOnSuccessListener(unused11 -> {
                                                                        Utils.Cache.removeKey(requireActivity().getApplicationContext(), "latitude");
                                                                        Utils.Cache.removeKey(requireActivity().getApplicationContext(), "longitude");
                                                                        Utils.Cache.removeKey(requireActivity().getApplicationContext(), "addressLine");
                                                                        Toast.makeText(requireActivity().getApplicationContext(), "Successfully submitted", Toast.LENGTH_SHORT).show();
                                                                        requireActivity().onBackPressed();
                                                                    });
                                                        }
                                                    });
                                        }
                                    });
                        }
                    }
                    else {
                        Utils.Cache.removeKey(requireActivity().getApplicationContext(), "latitude");
                        Utils.Cache.removeKey(requireActivity().getApplicationContext(), "longitude");
                        Utils.Cache.removeKey(requireActivity().getApplicationContext(), "addressLine");
                        Toast.makeText(requireActivity().getApplicationContext(), "Successfully submitted", Toast.LENGTH_SHORT).show();
                        requireActivity().onBackPressed();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        btnSubmit.setEnabled(true);
                    }
                });
    }

    private void uploadAppointmentFormData() {
        String firstName = etFirstName.getText().toString().toUpperCase();
        String lastName = etLastName.getText().toString().toUpperCase();
        String mobile = etMobile.getText().toString().toUpperCase();
        String brand = menuBrand.getText().toString().toUpperCase();
        String model = menuModel.getText().toString().toUpperCase();
        String serviceType = menuTypeOfService.getText().toString().toUpperCase();
        String description = menuIssueDescription.getText().toString().toUpperCase();
        long schedule = dpScheduleSelection;

        Map<String, Object> location = new HashMap<>();
        location.put("latitude", Utils.Cache.getDouble(requireContext(), "selected_latitude"));
        location.put("longitude", Utils.Cache.getDouble(requireContext(), "selected_longitude"));
        location.put("addressLine", Utils.Cache.getString(requireContext(), "addressLine"));

        Map<String, Object> appointment = new HashMap<>();
        appointment.put("userUid", AUTH.getUid());
        appointment.put("firstName", firstName);
        appointment.put("lastName", lastName);
        appointment.put("mobile", mobile);
        appointment.put("brand", brand);
        appointment.put("model", model);
        appointment.put("serviceType", serviceType);
        appointment.put("description", description);
        appointment.put("schedule", schedule);
        appointment.put("location", location);
        appointment.put("timestamp", System.currentTimeMillis());
        appointment.put("status", "PENDING");

        // upload all media to firebase storage
        DocumentReference refNewAppointment =  DB.collection("appointments").document();
        appointment.put("uid", refNewAppointment.getId());

        refNewAppointment.set(appointment)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Utils.Cache.removeKey(requireContext(), "latitude");
                        Utils.Cache.removeKey(requireContext(), "longitude");
                        Utils.Cache.removeKey(requireContext(), "addressLine");
                        Toast.makeText(requireActivity().getApplicationContext(), "Your appointment has been booked", Toast.LENGTH_SHORT).show();
                        requireActivity().onBackPressed();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        btnSubmit.setEnabled(true);
                    }
                });
    }

    private void calculateTotal() {
        float cartSubTotal = 0;
        for (int i = 0; i < arrCartItem.size(); i++) {

            float qty = arrCartItem.get(i).getQuantity();
            double price = arrProducts.get(i).getPrice();
            double subTotal = qty * price;
            cartSubTotal += subTotal;
        }

        DecimalFormat df = new DecimalFormat("0.00");
        tvCartSubtotal.setText("Cart Subtotal: ₱"+df.format(cartSubTotal));

        if (Utils.Cache.getBoolean(requireContext(), "sos_mode")) {
            tvServiceFee.setText("Service Fee: ₱"+df.format(selectedServiceFee));
            tvTotal.setText("Total: ₱"+df.format(cartSubTotal + selectedServiceFee));
        }
        else {
            tvServiceFee.setText("Service Fee: To be determined");
            tvTotal.setText("Total: To be determined");
        }
    }
}