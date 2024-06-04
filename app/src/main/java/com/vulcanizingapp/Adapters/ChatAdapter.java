package com.vulcanizingapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.vulcanizingapp.Objects.Chat;
import com.vulcanizingapp.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.chatViewHolder>{

    FirebaseUser USER =  FirebaseAuth.getInstance().getCurrentUser();

    Context context;
    ArrayList<Chat> chatArrayList;
    private OnChatListener mOnChatListener;

    public ChatAdapter(Context context, ArrayList<Chat> chatArrayList, OnChatListener onChatListener) {
        this.context = context;
        this.chatArrayList = chatArrayList;
        this.mOnChatListener = onChatListener;
    }

    @NonNull
    @Override
    public chatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_chat, parent, false);
        return new chatViewHolder(view, mOnChatListener);
    }

    @Override
    public void onBindViewHolder(@NonNull chatViewHolder holder, int position) {
        Chat chat = chatArrayList.get(position);
        String id = chat.getId(); // message uid
        String authorUid = chat.getAuthorUid(); // message author
        String message = chat.getMessage(); // message content
        long timestamp = chat.getTimestamp(); // time sent in milliseconds
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm aa"); // time sent in hh:mm aa format

        // check which POV to show
        boolean AUTHOR_IS_CURRENT_USER = Objects.equals(authorUid, USER.getUid());
        if (AUTHOR_IS_CURRENT_USER) {
            holder.cl1stPerson.setVisibility(View.VISIBLE);
            holder.cl2ndPerson.setVisibility(View.GONE);
        }
        else {
            holder.cl1stPerson.setVisibility(View.GONE);
            holder.cl2ndPerson.setVisibility(View.VISIBLE);
        }

        // for 2nd person, check if previous messages are from 2nd person
        // if so, hide the profile picture and to leave the lowest item's profile picture visible
        if (!AUTHOR_IS_CURRENT_USER) {
            if ((position!=0) && (chatArrayList.get(position-1).getAuthorUid().equals(authorUid))){
                holder.imgProfile2ndPerson.setVisibility(View.INVISIBLE);
            }
            else {
                holder.imgProfile2ndPerson.setVisibility(View.VISIBLE);
            }
        }

        holder.tvMessage1stPerson.setText(message);
        holder.tvMessage2ndPerson.setText(message);

        holder.tvTimestamp1stPerson.setVisibility(View.GONE);
        holder.tvTimestamp2ndPerson.setVisibility(View.GONE);

        long dayDifference = getTimeDifference(timestamp);
        if (dayDifference == 0){
            holder.tvTimestamp1stPerson.setText(simpleDateFormat.format(timestamp));
            holder.tvTimestamp2ndPerson.setText(simpleDateFormat.format(timestamp));
        }
        else if (dayDifference == 1) {
            holder.tvTimestamp1stPerson.setText("Yesterday at "+simpleDateFormat.format(timestamp));
            holder.tvTimestamp2ndPerson.setText("Yesterday at "+simpleDateFormat.format(timestamp));
        }
        else if (dayDifference > 1){
            // get message date
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp);
            long day = calendar.get(Calendar.DAY_OF_MONTH);
            String month = new SimpleDateFormat("MMM").format(calendar.getTime());
            long year = calendar.get(Calendar.YEAR);
            holder.tvTimestamp1stPerson.setText(month + " " + day + ", " + year + " at "+simpleDateFormat.format(timestamp));
            holder.tvTimestamp2ndPerson.setText(month + " " + day + ", " + year + " at "+simpleDateFormat.format(timestamp));
        }
    }

    private long getTimeDifference(long timestamp) {
        // check message age
        Calendar calNow, calMessage;

        calNow = Calendar.getInstance();
        calMessage = Calendar.getInstance();

        calNow.setTimeInMillis(System.currentTimeMillis());
        calMessage.setTimeInMillis(timestamp);

        long dayNow = calNow.get(Calendar.DAY_OF_MONTH);
        long dayMessage = calMessage.get(Calendar.DAY_OF_MONTH);

        return dayNow - dayMessage;
    }

    @Override
    public int getItemCount() {
        return chatArrayList.size();
    }

    public static class chatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ConstraintLayout cl1stPerson, cl2ndPerson;
        MaterialButton imgProfile2ndPerson;
        MaterialCardView cvChat1stPerson, cvChat2ndPerson;
        TextView tvTimestamp1stPerson, tvMessage1stPerson, tvTimestamp2ndPerson, tvMessage2ndPerson;

        OnChatListener onChatListener;
        public chatViewHolder(@NonNull View itemView, OnChatListener onChatListener) {
            super(itemView);
            imgProfile2ndPerson = itemView.findViewById(R.id.imgProfile2ndPerson);
            cl1stPerson = itemView.findViewById(R.id.cl1stPerson);
            cvChat1stPerson = itemView.findViewById(R.id.cvChat1stPerson);
            tvTimestamp1stPerson = itemView.findViewById(R.id.tvTimestamp1stPerson);
            tvMessage1stPerson = itemView.findViewById(R.id.tvMessage1stPerson);
            cl2ndPerson = itemView.findViewById(R.id.cl2ndPerson);
            cvChat2ndPerson = itemView.findViewById(R.id.cvChat2ndPerson);
            tvTimestamp2ndPerson = itemView.findViewById(R.id.tvTimestamp2ndPerson);
            tvMessage2ndPerson = itemView.findViewById(R.id.tvMessage2ndPerson);
            this.onChatListener = onChatListener;

            itemView.setOnClickListener(this);

            cvChat1stPerson.setOnClickListener(view -> {
                if (tvTimestamp1stPerson.getVisibility() == View.VISIBLE){
                    tvTimestamp1stPerson.setVisibility(View.GONE);
                }
                else {
                    tvTimestamp1stPerson.setVisibility(View.VISIBLE);
                }
            });

            cvChat2ndPerson.setOnClickListener(view -> {
                if (tvTimestamp2ndPerson.getVisibility() == View.VISIBLE){
                    tvTimestamp2ndPerson.setVisibility(View.GONE);
                }
                else {
                    tvTimestamp2ndPerson.setVisibility(View.VISIBLE);
                }
            });
        }

        @Override
        public void onClick(View view) {
            onChatListener.onChatClick(getAdapterPosition());
        }
    }

    public interface OnChatListener{
        void onChatClick(int position);
    }
}
