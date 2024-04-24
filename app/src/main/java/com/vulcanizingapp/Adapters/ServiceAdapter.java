package com.vulcanizingapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.vulcanizingapp.Objects.Service;
import com.vulcanizingapp.R;
import com.vulcanizingapp.Utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Objects;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.servicesViewHolder> {

    private static final FirebaseFirestore DB = FirebaseFirestore.getInstance();

    Context context;
    ArrayList<Service> arrServices;
    private OnServicesListener mOnServicesListener;

    public ServiceAdapter(Context context, ArrayList<Service> arrServices, OnServicesListener onServicesListener) {
        this.context = context;
        this.arrServices = arrServices;
        this.mOnServicesListener = onServicesListener;
    }

    @NonNull
    @Override
    public servicesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_service, parent, false);
        return new servicesViewHolder(view, mOnServicesListener);
    }

    @Override
    public void onBindViewHolder(@NonNull servicesViewHolder holder, int position) {
        Service services = arrServices.get(position);
        String serviceName = services.getServiceName();
        String description = services.getDescription();
        double price = services.getPrice();

        holder.tvServiceName.setText(serviceName);
        holder.tvDescription.setText(description);
        holder.tvPrice.setText("₱"+ Utils.DoubleFormatter.currencyFormat(price));
    }

    @Override
    public int getItemCount() {
        return arrServices.size();
    }

    public class servicesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        OnServicesListener onServicesListener;
        MaterialCardView cardService;
        TextView tvServiceName, tvDescription, tvPrice;

        public servicesViewHolder(@NonNull View itemView, OnServicesListener onServicesListener) {
            super(itemView);

            cardService = itemView.findViewById(R.id.cardService);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvPrice = itemView.findViewById(R.id.tvPrice);

            this.onServicesListener = onServicesListener;
            cardService.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onServicesListener.onServicesClick(getAdapterPosition());
        }
    }

    public interface OnServicesListener{
        void onServicesClick(int position);
    }
}
