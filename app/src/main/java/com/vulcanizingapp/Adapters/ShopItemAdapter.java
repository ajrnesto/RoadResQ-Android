package com.vulcanizingapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.vulcanizingapp.Objects.ShopItem;
import com.vulcanizingapp.R;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class ShopItemAdapter extends RecyclerView.Adapter<ShopItemAdapter.shopItemViewHolder> {

    private final StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    Context context;
    ArrayList<ShopItem> arrShopItem;
    private OnShopItemListener mOnShopItemListener;

    public ShopItemAdapter(Context context, ArrayList<ShopItem> arrShopItem, OnShopItemListener onShopItemListener) {
        this.context = context;
        this.arrShopItem = arrShopItem;
        this.mOnShopItemListener = onShopItemListener;
    }

    @NonNull
    @Override
    public shopItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_shop_item, parent, false);
        return new shopItemViewHolder(view, mOnShopItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull shopItemViewHolder holder, int position) {
        ShopItem shopItem = arrShopItem.get(position);

        String categoryId = shopItem.getCategoryId();
        double price = shopItem.getPrice();
        String productName = shopItem.getProductName();
        int stock = shopItem.getStock();
        Long thumbnail = shopItem.getThumbnail();

        DecimalFormat df = new DecimalFormat("0.00");
        holder.tvPrice.setText("₱"+df.format(price));
        holder.tvName.setText(productName);
        holder.tvStock.setText("Stock: " + stock);

        storageRef.child("products/"+thumbnail).getDownloadUrl()
                .addOnSuccessListener(uri -> Picasso.get().load(uri).resize(240,240).centerInside().into(holder.ivProduct))
                .addOnFailureListener(e -> Picasso.get().load("https://via.placeholder.com/150?text=No+Image").fit().into(holder.ivProduct));
    }

    @Override
    public int getItemCount() {
        return arrShopItem.size();
    }

    public class shopItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        OnShopItemListener onShopItemListener;
        AppCompatImageView ivProduct;
        TextView tvName, tvPrice, tvStock;

        public shopItemViewHolder(@NonNull View itemView, OnShopItemListener onShopItemListener) {
            super(itemView);

            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStock = itemView.findViewById(R.id.tvStock);

            this.onShopItemListener = onShopItemListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onShopItemListener.onShopItemClick(getAdapterPosition());
        }
    }

    public interface OnShopItemListener{
        void onShopItemClick(int position);
    }
}
