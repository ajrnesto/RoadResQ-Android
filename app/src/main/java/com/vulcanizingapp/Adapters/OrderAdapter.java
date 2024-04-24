package com.vulcanizingapp.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.vulcanizingapp.Objects.Order;
import com.vulcanizingapp.Objects.Product;
import com.vulcanizingapp.R;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Objects;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.orderViewHolder> {

    private final FirebaseFirestore DB = FirebaseFirestore.getInstance();
    private final FirebaseAuth AUTH = FirebaseAuth.getInstance();
    private final StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    Context context;
    ArrayList<Order> arrOrder;
    OrderItemAdapter orderItemsAdapter;
    private OnOrderListener mOnOrderListener;

    public OrderAdapter(Context context, ArrayList<Order> arrOrder, OnOrderListener onOrderListener) {
        this.context = context;
        this.arrOrder = arrOrder;
        this.mOnOrderListener = onOrderListener;
    }

    @NonNull
    @Override
    public orderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_order, parent, false);
        return new orderViewHolder(view, mOnOrderListener);
    }

    @Override
    public void onBindViewHolder(@NonNull orderViewHolder holder, int position) {
        Order order = arrOrder.get(position);

        String id = order.getId();
        ArrayList<String> deliveryAddress = order.getDeliveryAddress();
        String deliveryMethod = order.getDeliveryMethod();
        String status = order.getStatus();
        long timestamp = order.getTimestamp();
        double total = order.getTotal();
        ArrayList<Product> arrOrderItems = order.getArrOrderItems();

        if (deliveryAddress != null){
            holder.tvDeliveryAddress.setText("Delivery Address: "+String.join(", ", deliveryAddress));
        }
        if (deliveryMethod != null) {
            holder.tvDeliveryMethod.setText("Delivery Method: "+deliveryMethod);
        }
        if (Objects.equals(status, "Pending")) {
            holder.btnCancel.setVisibility(View.VISIBLE);
        }
        else {
            holder.btnCancel.setVisibility(View.GONE);
        }
        holder.tvStatus.setText(status);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy, hh:mm aa");
        holder.tvTimestamp.setText(sdf.format(timestamp));

        DecimalFormat df = new DecimalFormat("0.00");
        holder.tvTotal.setText("Total: ₱"+df.format(total));

        holder.rvOrderItems.setHasFixedSize(true);
        holder.rvOrderItems.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));

        orderItemsAdapter = new OrderItemAdapter(holder.itemView.getContext(), arrOrderItems);
        holder.rvOrderItems.setAdapter(orderItemsAdapter);
    }

    @Override
    public int getItemCount() {
        return arrOrder.size();
    }

    public class orderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        OnOrderListener onOrderListener;
        AppCompatImageView ivProduct;
        TextView tvTimestamp, tvStatus, tvDeliveryMethod, tvDeliveryAddress, tvTotal;
        MaterialButton btnCancel;
        RecyclerView rvOrderItems;

        public orderViewHolder(@NonNull View itemView, OnOrderListener onOrderListener) {
            super(itemView);

            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDeliveryMethod = itemView.findViewById(R.id.tvDeliveryMethod);
            tvDeliveryAddress = itemView.findViewById(R.id.tvDeliveryAddress);
            rvOrderItems = itemView.findViewById(R.id.rvOrderItems);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            btnCancel = itemView.findViewById(R.id.btnCancel);

            this.onOrderListener = onOrderListener;
            itemView.setOnClickListener(this);

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MaterialAlertDialogBuilder dialogDelete = new MaterialAlertDialogBuilder(itemView.getContext());
                    dialogDelete.setTitle("Cancel Order");
                    dialogDelete.setMessage("Please confirm that you want cancel this order");
                    dialogDelete.setNeutralButton("Back", (dialogInterface, i) -> {

                    });
                    dialogDelete.setNegativeButton("Cancel Order", (dialogInterface, i) -> {
                        for (Product orderItem : arrOrder.get(getAdapterPosition()).getArrOrderItems()) {
                            DB.collection("products").document(orderItem.getProductId())
                                    .update("stock", FieldValue.increment(orderItem.getQuantity()));
                        }
                        DB.collection("orders").document(arrOrder.get(getAdapterPosition()).getId())
                                .delete()
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(view.getContext(), "Successfully cancelled your order.", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(view.getContext(), "Failed to cancel order: "+e, Toast.LENGTH_SHORT).show();
                                });
                    });
                    dialogDelete.show();
                }
            });
        }

        @Override
        public void onClick(View view) {
            onOrderListener.onOrderClick(getAdapterPosition());
        }
    }

    public interface OnOrderListener{
        void onOrderClick(int position);
    }
}
