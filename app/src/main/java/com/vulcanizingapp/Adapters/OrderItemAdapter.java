package com.vulcanizingapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;
import com.vulcanizingapp.Objects.Product;
import com.vulcanizingapp.R;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.orderItemViewHolder> {

    private final StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    Context context;
    ArrayList<Product> arrOrderItems;

    public OrderItemAdapter(Context context, ArrayList<Product> arrOrderItems) {
        this.context = context;
        this.arrOrderItems = arrOrderItems;
    }

    @NonNull
    @Override
    public orderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_order_item, parent, false);
        return new orderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull orderItemViewHolder holder, int position) {
        Product orderItem = arrOrderItems.get(position);

        if (orderItem.getProductId() == "-1") {
            Picasso.get().load("https://via.placeholder.com/150?text=No+Image").fit().into(holder.ivProduct);
            holder.tvName.setText("Deleted Item");
            holder.tvPrice.setText("₱---.--");
            holder.tvQuantity.setText("");
            holder.tvDetails.setText("Deleted Item");
            holder.tvSubtotal.setText("₱---.--");
            return;
        }

        String productId = orderItem.getProductId();
        int quantity = orderItem.getQuantity();
        String productName = orderItem.getProductName();
        String productDetails = orderItem.getProductDetails();
        double price = orderItem.getPrice();
        Long thumbnail = orderItem.getThumbnail();

        DecimalFormat df = new DecimalFormat("0.00");
        holder.tvPrice.setText("₱"+df.format(price));
        holder.tvQuantity.setText("x"+quantity);
        holder.tvName.setText(productName);
        holder.tvDetails.setText(productDetails);

        double subtotal = Float.parseFloat(String.valueOf(quantity)) * price;
        holder.tvSubtotal.setText("₱"+df.format(subtotal));

        storageRef.child("products/"+thumbnail).getDownloadUrl()
                .addOnSuccessListener(uri -> Picasso.get().load(uri).resize(240,240).centerInside().into(holder.ivProduct))
                .addOnFailureListener(e -> Picasso.get().load("https://via.placeholder.com/150?text=Image").fit().into(holder.ivProduct));
    }

    @Override
    public int getItemCount() {
        return arrOrderItems.size();
    }

    public class orderItemViewHolder extends RecyclerView.ViewHolder {

        RoundedImageView ivProduct;
        TextView tvName, tvDetails, tvPrice, tvQuantity, tvSubtotal;

        public orderItemViewHolder(@NonNull View itemView) {
            super(itemView);

            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvName = itemView.findViewById(R.id.tvName);
            tvDetails = itemView.findViewById(R.id.tvDetails);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvSubtotal = itemView.findViewById(R.id.tvSubtotal);
        }
    }
}
