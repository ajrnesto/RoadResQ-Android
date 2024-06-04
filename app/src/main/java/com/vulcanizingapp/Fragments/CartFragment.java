package com.vulcanizingapp.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.vulcanizingapp.Adapters.CartItemAdapter;
import com.vulcanizingapp.Objects.CartItem;
import com.vulcanizingapp.Objects.Product;
import com.vulcanizingapp.Objects.ShopItem;
import com.vulcanizingapp.R;
import com.vulcanizingapp.Utils.Utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CartFragment extends Fragment implements CartItemAdapter.OnCartItemListener, CartItemAdapter.OnQuantityChanged, CartItemAdapter.OnItemDeleted {

    FirebaseFirestore DB;
    FirebaseAuth AUTH;
    FirebaseUser USER;
    Query qryCart;
    ListenerRegistration velCart;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }

    View view;

    TextView tvTotal;
    MaterialButton btnPlaceOrder;
    RecyclerView rvCart;
    ConstraintLayout clLoadingBar;

    ArrayList<CartItem> arrCartItem;
    ArrayList<ShopItem> arrProducts;
    CartItemAdapter cartItemAdapter;
    CartItemAdapter.OnCartItemListener onCartItemListener = this;
    CartItemAdapter.OnQuantityChanged onQuantityChanged = this;
    CartItemAdapter.OnItemDeleted onItemDeleted = this;

    View.OnClickListener placeOrderListener = view -> {
        btnPlaceOrder.setEnabled(false);

        if (arrCartItem.isEmpty()) {
            Utils.simpleDialog(requireContext(), "Empty Cart", "Your cart is empty, please add at least one (1) item to place an order.", "Okay");
            btnPlaceOrder.setEnabled(true);
            return;
        }
            /*CheckoutDialog checkoutDialog = new CheckoutDialog();
            Bundle args = new Bundle();
            args.putFloat("total", Float.parseFloat(tvTotal.getText().toString().substring(8)));
            checkoutDialog.setArguments(args);
            checkoutDialog.setData(arrCartItem, arrProducts);
            checkoutDialog.show(requireActivity().getSupportFragmentManager(), "CHECKOUT_DIALOG");*/
        uploadOrderData();
    };

    View.OnClickListener backToAppointmentFormListener = view -> {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        fragmentManager.popBackStack ("SHOP_FRAGMENT", FragmentManager.POP_BACK_STACK_INCLUSIVE);
    };

    @Override
    public void onResume() {
        super.onResume();

        if (Utils.Cache.getBoolean(requireContext(), "appointment_items_selection_mode")) {
            btnPlaceOrder.setText("Proceed");
            btnPlaceOrder.setOnClickListener(backToAppointmentFormListener);
        }
        else {
            btnPlaceOrder.setText("Checkout");
            btnPlaceOrder.setOnClickListener(placeOrderListener);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_cart, container, false);

        initializeFirebase();
        initializeViews();
        loadRecyclerView();

        return view;
    }

    private void uploadOrderData() {
        DocumentReference refNewOrder = DB.collection("orders").document();

        Map<String, Object> order = new HashMap<>();
        order.put("id", refNewOrder.getId());
        order.put("total", Float.parseFloat(tvTotal.getText().toString().substring(8)));
        order.put("deliveryOption", "Pick-up");
        order.put("deliveryAddress", Collections.singletonList("-"));
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
                                                                        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                                                                        FragmentTransaction fragmentTransaction= fragmentManager.beginTransaction();
                                                                        Fragment ordersFragment = new OrdersFragment();
                                                                        fragmentTransaction.replace(R.id.fragmentHolder, ordersFragment, "ORDERS_FRAGMENT");
                                                                        fragmentTransaction.addToBackStack("ORDERS_FRAGMENT");
                                                                        fragmentTransaction.commit();

                                                                        Toast.makeText(requireContext(), "Your order has been placed.", Toast.LENGTH_SHORT).show();
                                                                        btnPlaceOrder.setEnabled(true);
                                                                    }
                                                                });
                                                    }
                                                });
                                    }
                                });
                    }
                });
    }

    private void initializeViews() {
        rvCart = view.findViewById(R.id.rvCart);
        tvTotal = view.findViewById(R.id.tvTotal);
        btnPlaceOrder = view.findViewById(R.id.btnPlaceOrder);
        clLoadingBar = view.findViewById(R.id.clLoadingBar);
    }

    private void loadRecyclerView() {
        arrCartItem = new ArrayList<>();
        arrProducts = new ArrayList<>();
        rvCart = view.findViewById(R.id.rvCart);
        rvCart.setHasFixedSize(true);
        rvCart.setLayoutManager(new LinearLayoutManager(requireContext()));

        // empty snapshot listener
        DB.collection("carts").document(AUTH.getCurrentUser().getUid())
                .collection("items")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (value.isEmpty()) {
                            rvCart.setVisibility(View.INVISIBLE);
                            clLoadingBar.setVisibility(View.GONE);
                            btnPlaceOrder.setEnabled(false);
                            tvTotal.setText("Total: ₱0.00");
                        }
                        else {
                            rvCart.setVisibility(View.VISIBLE);
                            btnPlaceOrder.setEnabled(true);
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
                                            calculateTotal();
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

        cartItemAdapter = new CartItemAdapter(requireContext(), requireActivity(), arrCartItem, arrProducts, onCartItemListener, onQuantityChanged, onItemDeleted);
        rvCart.setAdapter(cartItemAdapter);
    }

    private void calculateTotal() {
        float total = 0;
        for (int i = 0; i < arrCartItem.size(); i++) {

            float qty = arrCartItem.get(i).getQuantity();
            double price = arrProducts.get(i).getPrice();
            double subTotal = qty * price;
            total += subTotal;
        }

        DecimalFormat df = new DecimalFormat("0.00");
        tvTotal.setText("Total: ₱"+df.format(total));
    }

    @Override
    public void onCartItemClick(int position) {
        /*Toast.makeText(requireContext(), "Clicked: "+position, Toast.LENGTH_SHORT).show();*/
    }

    @Override
    public void onQuantityChanged(int position, int quantity) {
        arrCartItem.get(position).setQuantity(quantity);

        calculateTotal();
    }

    @Override
    public void onItemDeleted(int position) {
        arrCartItem.remove(position);
        /*Objects.requireNonNull(rvCart.getAdapter()).notifyItemRemoved(position);
        Objects.requireNonNull(rvCart.getAdapter()).notifyItemRangeChanged(0, arrCartItem.size());*/

        clLoadingBar.setVisibility(View.VISIBLE);
        loadRecyclerView();

        calculateTotal();

        /*loadRecyclerView();*/

        /*arrCartItem.remove(position);
        rvCart.getAdapter().notifyDataSetChanged();*/
    }
}