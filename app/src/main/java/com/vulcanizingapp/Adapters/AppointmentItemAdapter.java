package com.vulcanizingapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.vulcanizingapp.Objects.CartItem;
import com.vulcanizingapp.Objects.ShopItem;
import com.vulcanizingapp.R;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class AppointmentItemAdapter extends RecyclerView.Adapter<AppointmentItemAdapter.shopItemViewHolder> {

    private final StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    Context context;
    ArrayList<ShopItem> arrShopItem;
    ArrayList<CartItem> arrCartItem;

    public AppointmentItemAdapter(Context context, ArrayList<ShopItem> arrShopItem, ArrayList<CartItem> arrCartItem) {
        this.context = context;
        this.arrShopItem = arrShopItem;
        this.arrCartItem = arrCartItem;
    }

    @NonNull
    @Override
    public shopItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_appointment_item, parent, false);
        return new shopItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull shopItemViewHolder holder, int position) {
        ShopItem shopItem = arrShopItem.get(position);
        CartItem cartItem = arrCartItem.get(position);

        String categoryId = shopItem.getCategoryId();
        double price = shopItem.getPrice();
        double quantity = cartItem.getQuantity();
        String productName = shopItem.getProductName();
        Long thumbnail = shopItem.getThumbnail();

        DecimalFormat df = new DecimalFormat("0.00");
        holder.tvPrice.setText("₱"+df.format(price));
        holder.tvQuantity.setText("x" + new DecimalFormat("#").format(quantity));
        holder.tvName.setText(productName);

        storageRef.child("products/"+thumbnail).getDownloadUrl()
                .addOnSuccessListener(uri -> Picasso.get().load(uri).resize(240,240).centerInside().into(holder.ivProduct))
                .addOnFailureListener(e -> Picasso.get().load("https://via.placeholder.com/150?text=No+Image").fit().into(holder.ivProduct));
    }

    @Override
    public int getItemCount() {
        return arrShopItem.size();
    }

    public class shopItemViewHolder extends RecyclerView.ViewHolder {
        AppCompatImageView ivProduct;
        TextView tvName, tvPrice, tvQuantity;

        public shopItemViewHolder(@NonNull View itemView) {
            super(itemView);

            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
        }
    }
}
