package com.vulcanizingapp.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.vulcanizingapp.Adapters.OrderAdapter;
import com.vulcanizingapp.Objects.Order;
import com.vulcanizingapp.Objects.Product;
import com.vulcanizingapp.R;

import java.util.ArrayList;
import java.util.Objects;

public class OrdersFragment extends Fragment implements OrderAdapter.OnOrderListener {

    FirebaseFirestore DB;
    FirebaseAuth AUTH;
    FirebaseUser USER;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }

    View view;

    TabLayout tabOrders;
    RecyclerView rvOrders;
    TextView tvEmpty;

    ArrayList<Order> arrOrders;
    ArrayList<Product> arrOrderItems;
    OrderAdapter orderAdapter;
    OrderAdapter.OnOrderListener onOrderListener = this;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_orders, container, false);

        initializeFirebase();
        initializeViews();
        loadRecyclerView(tabOrders.getSelectedTabPosition());

        tabOrders.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                loadRecyclerView(tabOrders.getSelectedTabPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        return view;
    }

    private void loadRecyclerView(int tabIndex) {
        arrOrders = new ArrayList<>();
        arrOrderItems = new ArrayList<>();
        rvOrders = view.findViewById(R.id.rvOrders);
        rvOrders.setHasFixedSize(true);
        rvOrders.setLayoutManager(new LinearLayoutManager(requireContext()));

        String statusFilter = "";
        if (tabIndex == 0) {
            statusFilter = "Pending";
        }
        else if (tabIndex == 1) {
            statusFilter = "Preparing";
        }
        else if (tabIndex == 2) {
            statusFilter = "Ready for Pick-up";
        }
        else if (tabIndex == 3) {
            statusFilter = "Completed";
        }

        Query qryMyOrders = DB.collection("orders")
                        .whereEqualTo("customer", Objects.requireNonNull(AUTH.getCurrentUser()).getUid())
                        .whereEqualTo("status", statusFilter);

        qryMyOrders.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        arrOrders.clear();

                        assert value != null;
                        if (value.isEmpty()) {
                            rvOrders.setVisibility(View.GONE);
                            tvEmpty.setVisibility(View.VISIBLE);
                        }
                        else {
                            rvOrders.setVisibility(View.VISIBLE);
                            tvEmpty.setVisibility(View.GONE);
                        }

                        for (QueryDocumentSnapshot doc : value) {
                            String id = (String) doc.getData().get("id");
                            String customer = (String) doc.getData().get("customer");
                            ArrayList<String> deliveryAddress = (ArrayList<String>) doc.getData().get("deliveryAddress");
                            String deliveryMethod = (String) doc.getData().get("deliveryMethod");
                            String status = (String) doc.getData().get("status");
                            long timestamp = Long.parseLong(doc.getData().get("timestamp").toString());
                            double total = Double.parseDouble(doc.getData().get("total").toString());
                            ArrayList<Product> arrOrderItems = new ArrayList<>();

                            arrOrders.add(new Order(
                                    id,
                                    customer,
                                    deliveryAddress,
                                    deliveryMethod,
                                    status,
                                    timestamp,
                                    total,
                                    arrOrderItems
                            ));

                            DB.collection("orders").document(id)
                                    .collection("orderItems")
                                    .get()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot item : task.getResult()) {

                                                String productId = (String) item.getData().get("productId");
                                                int quantity = Integer.parseInt(item.getData().get("quantity").toString());

                                                DB.collection("products").document(productId)
                                                        .get()
                                                        .addOnCompleteListener(task1 -> {
                                                            if (task1.isSuccessful()) {
                                                                DocumentSnapshot product = task1.getResult();

                                                                if (product.getData() == null) {
                                                                    arrOrderItems.add(new Product(
                                                                            "-1",
                                                                            0,
                                                                            "Deleted Item",
                                                                            "",
                                                                            0,
                                                                            null
                                                                    ));
                                                                    orderAdapter.notifyDataSetChanged();

                                                                    return;
                                                                }

                                                                String productName = (String) product.getData().get("productName");
                                                                String productDetails = (String) product.getData().get("productDetails");
                                                                double price = Double.parseDouble(product.getData().get("price").toString());

                                                                Long thumbnail = null;
                                                                if (product.getData().get("thumbnail") != null) {
                                                                    thumbnail = Long.parseLong(product.getData().get("thumbnail").toString());
                                                                }

                                                                arrOrderItems.add(new Product(
                                                                        productId,
                                                                        quantity,
                                                                        productName,
                                                                        productDetails,
                                                                        price,
                                                                        thumbnail
                                                                ));

                                                                orderAdapter.notifyDataSetChanged();
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }

                        if (arrOrders.isEmpty()) {
                            rvOrders.setVisibility(View.INVISIBLE);
                        }
                        else {
                            rvOrders.setVisibility(View.VISIBLE);
                        }
                    }
                });

        orderAdapter = new OrderAdapter(requireContext(), arrOrders, onOrderListener);
        rvOrders.setAdapter(orderAdapter);
    }

    private void initializeViews() {
        tabOrders = view.findViewById(R.id.tabOrders);
        rvOrders = view.findViewById(R.id.rvOrders);
        tvEmpty = view.findViewById(R.id.tvEmpty);
    }

    @Override
    public void onOrderClick(int position) {
        
    }
}