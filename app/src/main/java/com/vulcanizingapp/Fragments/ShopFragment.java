package com.vulcanizingapp.Fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.vulcanizingapp.Adapters.ShopItemAdapter;
import com.vulcanizingapp.Dialogs.ShopItemDialog;
import com.vulcanizingapp.Objects.ShopItem;
import com.vulcanizingapp.R;
import com.vulcanizingapp.Utils.Utils;

import java.util.ArrayList;
import java.util.Objects;

public class ShopFragment extends Fragment implements ShopItemAdapter.OnShopItemListener {

    FirebaseFirestore DB;
    FirebaseAuth AUTH;
    FirebaseUser USER;
    Query qryShop;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }
    View view;

    TextInputLayout tilSearch;
    TextInputEditText etSearch;
    MaterialButton btnGoBackToAppointmentForm, btnRescue, btnOrders, btnCart;
    RecyclerView rvShop;

    ArrayList<ShopItem> arrShopItem;
    ShopItemAdapter ordersAdapter;
    ShopItemAdapter.OnShopItemListener onShopItemListener = this;

    @Override
    public void onResume() {
        super.onResume();
        if (Utils.Cache.getBoolean(requireContext(), "appointment_items_selection_mode")) {
            btnGoBackToAppointmentForm.setVisibility(View.VISIBLE);
            btnRescue.setVisibility(View.GONE);
        }
        else {
            btnGoBackToAppointmentForm.setVisibility(View.GONE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_shop, container, false);

        initializeFirebase();
        initializeViews();
        loadRecyclerView(null);
        handleUserInteraction();

        return view;
    }

    private void initializeViews() {
        Utils.Cache.setBoolean(requireContext(), "shop_item_dialog_is_visible", false);
        etSearch = view.findViewById(R.id.etSearch);
        tilSearch = view.findViewById(R.id.tilSearch);
        btnOrders = view.findViewById(R.id.btnOrders);
        btnCart = view.findViewById(R.id.btnCart);
        rvShop = view.findViewById(R.id.rvShop);
        btnGoBackToAppointmentForm = view.findViewById(R.id.btnGoBackToAppointmentForm);
        btnRescue = view.findViewById(R.id.btnRescue);
    }

    private void loadRecyclerView(String searchKey) {
        arrShopItem = new ArrayList<>();
        rvShop = view.findViewById(R.id.rvShop);
        rvShop.setHasFixedSize(true);
        /*LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        rvShop.setLayoutManager(linearLayoutManager);*/

        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 2);
        rvShop.setLayoutManager(gridLayoutManager);

        if (searchKey == null || searchKey.isEmpty()) {
            qryShop = DB.collection("products");
        }
        else {
            qryShop = DB.collection("products")
                    .orderBy("productNameAllCaps")
                    .startAt(searchKey)
                    .endAt(searchKey+'\uf8ff');
        }

        qryShop.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.d("DEBUG", "Listen failed.", error);
                    return;
                }

                arrShopItem.clear();

                for (QueryDocumentSnapshot doc : value) {
                    arrShopItem.add(doc.toObject(ShopItem.class));
                    ordersAdapter.notifyDataSetChanged();
                }
            }
        });

        ordersAdapter = new ShopItemAdapter(requireContext(), arrShopItem, onShopItemListener);
        rvShop.setAdapter(ordersAdapter);
        ordersAdapter.notifyDataSetChanged();
    }

    private void handleUserInteraction() {
        tilSearch.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.hideKeyboard(requireActivity());
                loadRecyclerView(Objects.requireNonNull(etSearch.getText()).toString().toUpperCase());
            }
        });

        etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    Utils.hideKeyboard(requireActivity());
                    loadRecyclerView(Objects.requireNonNull(etSearch.getText()).toString().toUpperCase());
                    return true;
                }
                return false;
            }
        });

        btnOrders.setOnClickListener(view -> {
            if (USER == null){
                Utils.loginRequiredDialog(requireContext(), requireActivity().findViewById(R.id.bottom_navbar), "You need to be logged in to view your order history");
                return;
            }

            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction= fragmentManager.beginTransaction();

            Fragment ordersFragment = new OrdersFragment();
            fragmentTransaction.replace(R.id.fragmentHolder, ordersFragment, "ORDERS_FRAGMENT");
            fragmentTransaction.addToBackStack("ORDERS_FRAGMENT");
            fragmentTransaction.commit();
        });

        btnRescue.setOnClickListener(view -> {
            btnRescue.setEnabled(false);
            Utils.Cache.setBoolean(requireContext(), "sos_mode", true);

            DB.collection("rescue").document(AUTH.getCurrentUser().getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot rescue = task.getResult();
                                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                                if (!rescue.exists() || Objects.equals(rescue.getString("status"), "COMPLETED")) {
                                    Fragment formAppointmentFragment = new FormAppointmentFragment();
                                    Bundle args = new Bundle();
                                    args.putString("selected_service", "Rescue");
                                    args.putDouble("selected_service_fee", 0);
                                    formAppointmentFragment.setArguments(args);
                                    fragmentTransaction.replace(R.id.fragmentHolder, formAppointmentFragment, "APPOINTMENT_FORM_FRAGMENT");
                                    fragmentTransaction.addToBackStack("APPOINTMENT_FORM_FRAGMENT");
                                    fragmentTransaction.commit();
                                }
                                else {
                                    Fragment waitRescueFragment = new WaitRescueFragment();
                                    Bundle args = new Bundle();
                                    waitRescueFragment.setArguments(args);
                                    fragmentTransaction.replace(R.id.fragmentHolder, waitRescueFragment, "WAIT_RESCUE_FRAGMENT");
                                    fragmentTransaction.addToBackStack("WAIT_RESCUE_FRAGMENT");
                                    fragmentTransaction.commit();
                                }
                            }
                            else {
                                btnRescue.setEnabled(true);
                            }
                        }
                    });
        });

        btnCart.setOnClickListener(view -> {
            if (USER == null){
                Utils.loginRequiredDialog(requireContext(), requireActivity().findViewById(R.id.bottom_navbar), "You need to be logged in to view your cart");
                return;
            }

            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction= fragmentManager.beginTransaction();

            Fragment cartFragment = new CartFragment();
            fragmentTransaction.replace(R.id.fragmentHolder, cartFragment, "CART_FRAGMENT");
            fragmentTransaction.addToBackStack("CART_FRAGMENT");
            fragmentTransaction.commit();
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                Utils.hideKeyboard(requireActivity());
                loadRecyclerView(Objects.requireNonNull(etSearch.getText()).toString().toUpperCase());
                return true;
            }
            return false;
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().isEmpty()) {
                    loadRecyclerView(Objects.requireNonNull(etSearch.getText()).toString().toUpperCase());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        btnGoBackToAppointmentForm.setOnClickListener(view -> requireActivity().onBackPressed());
    }

    @Override
    public void onShopItemClick(int position) {
        if (USER == null){
            Utils.loginRequiredDialog(requireContext(), requireActivity().findViewById(R.id.bottom_navbar), "You need to be logged in to place orders");
            return;
        }

        if (!Utils.Cache.getBoolean(requireContext(), "shop_item_dialog_is_visible")) {
            ShopItemDialog shopItemDialog = new ShopItemDialog();

            ShopItem shopItem = arrShopItem.get(position);
            Bundle args = new Bundle();
            args.putString("id", shopItem.getId());
            args.putDouble("price", shopItem.getPrice());
            args.putString("productName", shopItem.getProductName());
            args.putInt("stock", shopItem.getStock());
            if (shopItem.getThumbnail() != null) {
                args.putLong("thumbnail", shopItem.getThumbnail());
            }
            shopItemDialog.setArguments(args);
            shopItemDialog.show(requireActivity().getSupportFragmentManager(), "SHOP_ITEM_DIALOG");

            Utils.Cache.setBoolean(requireContext(), "shop_item_dialog_is_visible", true);

            /*FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment shopItemFragment = new ShopItemFragment();
            shopItemFragment.setArguments(args);
            fragmentTransaction.replace(R.id.fragmentHolder, shopItemFragment, "SHOP_ITEM_FRAGMENT");
            fragmentTransaction.addToBackStack("SHOP_ITEM_FRAGMENT");
            fragmentTransaction.commit();*/
        }
    }
}