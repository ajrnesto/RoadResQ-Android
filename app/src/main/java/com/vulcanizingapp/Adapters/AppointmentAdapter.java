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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.vulcanizingapp.Objects.Appointment;
import com.vulcanizingapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vulcanizingapp.Utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Objects;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.appointmentsViewHolder> {

    private static final FirebaseFirestore DB = FirebaseFirestore.getInstance();

    Context context;
    ArrayList<Appointment> arrAppointments;
    private OnAppointmentsListener mOnAppointmentsListener;

    public AppointmentAdapter(Context context, ArrayList<Appointment> arrAppointments, OnAppointmentsListener onAppointmentsListener) {
        this.context = context;
        this.arrAppointments = arrAppointments;
        this.mOnAppointmentsListener = onAppointmentsListener;
    }

    @NonNull
    @Override
    public appointmentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_appointment, parent, false);
        return new appointmentsViewHolder(view, mOnAppointmentsListener);
    }

    @Override
    public void onBindViewHolder(@NonNull appointmentsViewHolder holder, int position) {
        Appointment appointments = arrAppointments.get(position);
        String uid = appointments.getUid();
        String userUid = appointments.getUserUid();
        String firstName = appointments.getFirstName();
        String lastName = appointments.getLastName();
        String mobile = appointments.getMobile();
        String brand = appointments.getBrand();
        String model = appointments.getModel();
        String serviceType = appointments.getServiceType();
        String description = appointments.getDescription();
        long schedule = appointments.getSchedule();
        long timestamp = appointments.getTimestamp();
        String status = appointments.getStatus();

        holder.tvServiceType.setText(serviceType);
        holder.tvDescription.setText(description);
        holder.tvBrand.setText(brand);
        holder.tvModel.setText(model);
        loadTimestamp(holder, schedule);
        loadStatus(holder, status);
        loadPrice(holder, serviceType);
    }

    private void loadPrice(appointmentsViewHolder holder, String serviceType) {
        DB.collection("services").whereEqualTo("serviceNameAllCaps", serviceType)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot snapshot = task.getResult();

                            double price = snapshot.getDocuments().get(0).getDouble("price");
                            holder.tvPrice.setText("₱"+ Utils.DoubleFormatter.currencyFormat(price));
                        }
                    }
                });
    }

    private void loadTimestamp(appointmentsViewHolder holder, long schedule) {
        SimpleDateFormat sdfTimestamp = new SimpleDateFormat("MMMM dd, yyyy, hh:mm aa");
        holder.tvTimestamp.setText(sdfTimestamp.format(schedule));
    }

    private void loadStatus(appointmentsViewHolder holder, String status) {
        if (Objects.equals(status, "PENDING")) {
            holder.tvStatus.setText("Pending");
            holder.tvStatus.setTextColor(context.getColor(R.color.auburn));
            holder.btnCancel.setVisibility(View.VISIBLE);
        }
        else if (Objects.equals(status, "COMPLETED")) {
            holder.tvStatus.setText("Completed");
            holder.tvStatus.setTextColor(context.getColor(R.color.dark_moss_green));
            holder.btnCancel.setVisibility(View.GONE);
        }
        else if (Objects.equals(status, "DECLINED")) {
            holder.tvStatus.setText("Declined");
            holder.tvStatus.setTextColor(context.getColor(R.color.ash_gray));
            holder.btnCancel.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return arrAppointments.size();
    }

    public class appointmentsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        OnAppointmentsListener onAppointmentsListener;
        TextView tvStatus, tvTimestamp, tvServiceType, tvDescription, tvPrice, tvBrand, tvModel;
        MaterialButton btnCancel;

        public appointmentsViewHolder(@NonNull View itemView, OnAppointmentsListener onAppointmentsListener) {
            super(itemView);

            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvServiceType = itemView.findViewById(R.id.tvServiceType);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvBrand = itemView.findViewById(R.id.tvBrand);
            tvModel = itemView.findViewById(R.id.tvModel);
            btnCancel = itemView.findViewById(R.id.btnCancel);

            this.onAppointmentsListener = onAppointmentsListener;
            itemView.setOnClickListener(this);

            btnCancel.setOnClickListener(view -> {
                MaterialAlertDialogBuilder dialogCancel = new MaterialAlertDialogBuilder(itemView.getContext());
                dialogCancel.setTitle("Cancel appointment");
                dialogCancel.setMessage("Are you sure you want to cancel your service appointment?");
                dialogCancel.setPositiveButton("Cancel Appointment", (dialogInterface, i) -> {
                    int position = getAdapterPosition();
                    Appointment currentAppointment = arrAppointments.get(position);
                    String appointmentUid = currentAppointment.getUid();

                    DB.collection("appointments").document(appointmentUid).delete();
                    notifyDataSetChanged();
                });
                dialogCancel.setNeutralButton("Close", (dialogInterface, i) -> {

                });
                dialogCancel.show();
            });
        }

        @Override
        public void onClick(View view) {
            onAppointmentsListener.onAppointmentsClick(getAdapterPosition());
        }
    }

    public interface OnAppointmentsListener{
        void onAppointmentsClick(int position);
    }
}
