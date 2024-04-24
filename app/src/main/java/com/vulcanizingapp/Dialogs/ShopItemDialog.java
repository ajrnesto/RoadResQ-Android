package com.vulcanizingapp.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.appcompat.widget.AppCompatImageView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.vulcanizingapp.AuthenticationActivity;
import com.vulcanizingapp.R;
import com.vulcanizingapp.Utils.Utils;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ShopItemDialog extends AppCompatDialogFragment {

    private final StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    FirebaseFirestore DB;
    FirebaseAuth AUTH;
    FirebaseUser USER;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }

    String id;
    double price;
    String productDetails;
    String productName;
    int stock;
    Long thumbnail;

    AppCompatImageView ivProduct;
    TextView tvName, tvDetails, tvPrice, tvStock, tvTotal;
    MaterialButton btnDecrement, btnIncrement, btnAddToCart;
    TextInputEditText etQuantity;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_shop_item, null);

        initializeFirebase();
        initiate(view);
        loadItem();
        buttonHandler();

        etQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                calculateTotal();

                if (Objects.requireNonNull(etQuantity.getText()).toString().isEmpty()) {
                    return;
                }

                float quantity = Float.parseFloat(etQuantity.getText().toString());

                if (quantity > stock) {
                    etQuantity.setText(""+stock);
                    etQuantity.setSelection(etQuantity.length());
                }

                if (quantity < 1) {
                    etQuantity.setText(""+1);
                    etQuantity.setSelection(etQuantity.length());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);

        Utils.Cache.setBoolean(requireContext(), "shop_item_dialog_is_visible", false);
    }

    private void initiate(View view) {
        ivProduct = view.findViewById(R.id.ivProduct);
        tvName = view.findViewById(R.id.tvName);
        tvDetails = view.findViewById(R.id.tvDetails);
        tvPrice = view.findViewById(R.id.tvPrice);
        tvStock = view.findViewById(R.id.tvStock);
        tvTotal = view.findViewById(R.id.tvTotal);
        btnDecrement = view.findViewById(R.id.btnDecrement);
        btnIncrement = view.findViewById(R.id.btnIncrement);
        btnAddToCart = view.findViewById(R.id.btnAddToCart);
        etQuantity = view.findViewById(R.id.etQuantity);
    }

    private void loadItem() {
        assert getArguments() != null;
        id = getArguments().getString("id");
        Log.d("DEBUG", "RECEIVED ID: "+id);
        price = getArguments().getDouble("price");
        productDetails = getArguments().getString("productDetails");
        productName = getArguments().getString("productName");
        stock = getArguments().getInt("stock");
        thumbnail = getArguments().getLong("thumbnail");

        if (stock < 1) {
            btnIncrement.setEnabled(false);
            btnDecrement.setEnabled(false);
            etQuantity.setEnabled(false);
            btnAddToCart.setEnabled(false);
            tvStock.setTextColor(getResources().getColor(R.color.auburn));
            etQuantity.setText("0");
        }

        tvName.setText(productName);
        tvDetails.setText(productDetails);
        DecimalFormat df = new DecimalFormat("0.00");
        tvPrice.setText("₱"+df.format(price));
        tvStock.setText("Stock: "+stock);
        storageRef.child("products/"+thumbnail).getDownloadUrl()
                .addOnSuccessListener(uri -> Picasso.get().load(uri).resize(120,0).centerInside().into(ivProduct))
                .addOnFailureListener(e -> Picasso.get().load("https://via.placeholder.com/150?text=No+Image").fit().into(ivProduct));

        calculateTotal();
    }

    private void calculateTotal() {
        if (Objects.requireNonNull(etQuantity.getText()).toString().isEmpty()) {
            return;
        }

        float quantity = Float.parseFloat(etQuantity.getText().toString());
        float total = (float) (quantity * price);

        DecimalFormat df = new DecimalFormat("0.00");
        tvTotal.setText("₱"+df.format(total));
    }

    private void buttonHandler() {
        btnDecrement.setOnClickListener(view -> {
            int quantity;

            if (Objects.requireNonNull(etQuantity.getText()).toString().isEmpty()) {
                quantity = 1;
            }
            else {
                quantity = Integer.parseInt(etQuantity.getText().toString());
            }

            if (quantity > 1) {
                quantity--;
                etQuantity.setText(""+quantity);
                etQuantity.setSelection(etQuantity.length());
                calculateTotal();
            }
        });

        btnIncrement.setOnClickListener(view -> {
            int quantity;

            if (Objects.requireNonNull(etQuantity.getText()).toString().isEmpty()) {
                quantity = 1;
            }
            else {
                quantity = Integer.parseInt(etQuantity.getText().toString());
            }

            if (quantity < stock) {
                quantity++;
                etQuantity.setText(""+quantity);
                etQuantity.setSelection(etQuantity.length());
                calculateTotal();
            }
        });

        btnAddToCart.setOnClickListener(view -> {
            if (AUTH.getCurrentUser() != null) {
                btnAddToCart.setEnabled(false);
                Map<String, Object> cartItem = new HashMap<>();
                int quantity = Integer.parseInt(Objects.requireNonNull(etQuantity.getText()).toString());
                cartItem.put("productId", id);
                cartItem.put("quantity", quantity);

                // check if item exists
                DocumentReference refItem = DB.collection("carts").document(Objects.requireNonNull(AUTH.getCurrentUser()).getUid())
                        .collection("items").document(id);

                refItem.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            int snapshotQuantity = Integer.parseInt(Objects.requireNonNull(documentSnapshot.get("quantity")).toString());

                            int newQuantity = snapshotQuantity + quantity;
                            cartItem.put("quantity", newQuantity);

                            DB.collection("carts").document(Objects.requireNonNull(AUTH.getCurrentUser()).getUid())
                                    .collection("items").document(id)
                                    .set(cartItem, SetOptions.merge())
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(requireContext(), "Item added to cart", Toast.LENGTH_SHORT).show();
                                        requireDialog().dismiss();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(requireContext(), "Something went wrong...", Toast.LENGTH_SHORT).show();
                                        Toast.makeText(requireContext(), "Failed to add item to cart", Toast.LENGTH_SHORT).show();
                                    });

                        }
                        else {
                            DB.collection("carts").document(Objects.requireNonNull(AUTH.getCurrentUser()).getUid())
                                .collection("items").document(id)
                                .set(cartItem, SetOptions.merge())
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(requireActivity().getApplicationContext(), "Item added to cart", Toast.LENGTH_SHORT).show();
                                    requireDialog().dismiss();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(requireContext().getApplicationContext(), "Something went wrong...", Toast.LENGTH_SHORT).show();
                                    Toast.makeText(requireContext().getApplicationContext(), "Failed to add item to cart", Toast.LENGTH_SHORT).show();
                                });
                        }
                    }
                });

                /**/
            }
            else {
                MaterialAlertDialogBuilder dialogLoginRequired = new MaterialAlertDialogBuilder(requireContext());
                dialogLoginRequired.setTitle("Sign in required");
                dialogLoginRequired.setMessage("You need to sign in to place orders.");
                dialogLoginRequired.setPositiveButton("Log in", (dialogInterface, i) -> {
                    requireContext().startActivity(new Intent(requireContext(), AuthenticationActivity.class));
                    ((Activity)requireContext()).finish();
                });
                dialogLoginRequired.setNeutralButton("Back", (dialogInterface, i) -> { });
                dialogLoginRequired.show();
            }
        });
    }
}
