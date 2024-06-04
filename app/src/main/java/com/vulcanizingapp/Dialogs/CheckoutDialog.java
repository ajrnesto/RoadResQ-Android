package com.vulcanizingapp.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.vulcanizingapp.Fragments.OrdersFragment;
import com.vulcanizingapp.Fragments.ShopFragment;
import com.vulcanizingapp.Objects.CartItem;
import com.vulcanizingapp.Objects.ShopItem;
import com.vulcanizingapp.R;
import com.vulcanizingapp.Utils.Utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CheckoutDialog extends AppCompatDialogFragment {

    private ArrayList<CartItem> arrCartItem;
    private ArrayList<ShopItem> arrProducts;

    public void setData(ArrayList<CartItem> arrCartItem, ArrayList<ShopItem> arrProducts) {
        this.arrCartItem = arrCartItem;
        this.arrProducts = arrProducts;
    }

    private final StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    FirebaseFirestore DB;
    FirebaseAuth AUTH;
    FirebaseUser USER;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }

    ConstraintLayout clDeliveryAddress;
    RadioGroup radioGroup;
    MaterialRadioButton radDelivery, radPickup;
    TextInputEditText etPurok, etBarangay, etCity;
    TextView tvTotal;
    MaterialButton btnPlaceOrder;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_checkout, null);

        initializeFirebase();
        initiate(view);

        radioGroup.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            if (checkedId == radPickup.getId()) {
                clDeliveryAddress.setVisibility(View.GONE);
            }
            else if (checkedId == radDelivery.getId()) {
                clDeliveryAddress.setVisibility(View.VISIBLE);
            }
        });

        btnPlaceOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String purok = etPurok.getText().toString();
                String barangay = etBarangay.getText().toString();
                String city = etCity.getText().toString();

                if (radDelivery.isChecked()) {
                    if (purok.isEmpty() || barangay.isEmpty() || city.isEmpty()) {
                        Utils.simpleDialog(requireContext(), "Incomplete Address", "Please fill in all fields required for your delivery address.", "Okay");
                        return;
                    }
                }

                DocumentReference refNewOrder = DB.collection("orders").document();

                Map<String, Object> order = new HashMap<>();
                order.put("id", refNewOrder.getId());
                order.put("total", getArguments().getFloat("total"));
                if (radDelivery.isChecked()) {
                    order.put("deliveryOption", "Delivery");
                    order.put("deliveryAddress", Arrays.asList(purok, barangay, city));
                }
                else if (radPickup.isChecked()) {
                    order.put("deliveryOption", "Pick-up");
                    order.put("deliveryAddress", Collections.singletonList("-"));
                }
                order.put("customer", AUTH.getCurrentUser().getUid());
                order.put("status", "Pending");
                order.put("timestamp", System.currentTimeMillis());

                refNewOrder.set(order)
                        .addOnSuccessListener(unused -> {
                            for (CartItem cartItem : arrCartItem) {
                                DB.collection("products").document(cartItem.getProductId())
                                        .update("stock", FieldValue.increment(-cartItem.getQuantity()))
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Map<String, Object> orderItems = new HashMap<>();
                                                orderItems.put("productId", cartItem.getProductId());
                                                orderItems.put("quantity", cartItem.getQuantity());

                                                DB.collection("orders").document(refNewOrder.getId())
                                                        .collection("orderItems").document(cartItem.getProductId())
                                                        .set(orderItems).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                DB.collection("carts").document(AUTH.getCurrentUser().getUid())
                                                                        .collection("items").document(cartItem.getProductId())
                                                                        .delete()
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void unused) {
                                                                                getDialog().dismiss();

                                                                                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                                                                                FragmentTransaction fragmentTransaction= fragmentManager.beginTransaction();
                                                                                Fragment ordersFragment = new OrdersFragment();
                                                                                fragmentTransaction.replace(R.id.fragmentHolder, ordersFragment, "ORDERS_FRAGMENT");
                                                                                fragmentTransaction.addToBackStack("ORDERS_FRAGMENT");
                                                                                fragmentTransaction.commit();

                                                                                Toast.makeText(requireContext(), "Your order has been placed.", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        });
                                                            }
                                                        });
                                            }
                                        });
                            }
                        });
            }
        });

        builder.setView(view);
        return builder.create();
    }

    private void initiate(View view) {
        clDeliveryAddress = view.findViewById(R.id.clDeliveryAddress);
        radioGroup = view.findViewById(R.id.radioGroup);
        radDelivery = view.findViewById(R.id.radDelivery);
        radPickup = view.findViewById(R.id.radPickup);
        etPurok = view.findViewById(R.id.etPurok);
        etBarangay = view.findViewById(R.id.etBarangay);
        etCity = view.findViewById(R.id.etCity);
        tvTotal = view.findViewById(R.id.tvTotal);
        btnPlaceOrder = view.findViewById(R.id.btnPlaceOrder);

        float total = getArguments().getFloat("total");
        DecimalFormat df = new DecimalFormat("0.00");
        tvTotal.setText("Total: ₱"+df.format(total));
    }
}
