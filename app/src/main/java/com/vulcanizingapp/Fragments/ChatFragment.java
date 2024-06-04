package com.vulcanizingapp.Fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.vulcanizingapp.Adapters.ChatAdapter;
import com.vulcanizingapp.Objects.Chat;
import com.vulcanizingapp.R;

import java.util.ArrayList;

public class ChatFragment extends Fragment implements ChatAdapter.OnChatListener {

    FirebaseFirestore DB;
    FirebaseAuth AUTH;
    FirebaseUser USER;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }
    View view;
    RecyclerView rvChat;
    MaterialButton btnSend;
    TextInputEditText etChatBox;
    ChatAdapter chatAdapter;
    ChatAdapter.OnChatListener onChatListener = this;
    ArrayList<Chat> chatArrayList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_chat, container, false);

        initializeFirebase();
        initializeViews();
        handleUserInteraction();
        loadChat();

        return view;
    }

    private void initializeViews() {
        rvChat = view.findViewById(R.id.rvChat);
        btnSend = view.findViewById(R.id.btnSend);
        etChatBox = view.findViewById(R.id.etChatBox);
    }

    private void handleUserInteraction() {
        btnSend.setOnClickListener(view -> {
            String message = etChatBox.getText().toString().trim();

            if (!TextUtils.isEmpty(message)){
                DocumentReference refNewChat = DB.collection("chats").document(USER.getUid()).collection("chats").document();

                Chat chat = new Chat();
                chat.setId(refNewChat.getId());
                chat.setMessage(message);
                chat.setAuthorUid(USER.getUid());
                chat.setTimestamp(System.currentTimeMillis());

                refNewChat.set(chat);
                etChatBox.getText().clear();
            }
        });
    }

    private void loadChat() {
        rvChat.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        linearLayoutManager.setStackFromEnd(true);
        //linearLayoutManager.setReverseLayout(true);
        rvChat.setLayoutManager(linearLayoutManager);

        chatArrayList = new ArrayList<>();
        chatAdapter = new ChatAdapter(requireContext(), chatArrayList, onChatListener);
        rvChat.setAdapter(chatAdapter);

        Query qryChats = DB.collection("chats").document(AUTH.getUid())
                .collection("chats").orderBy("timestamp", Query.Direction.ASCENDING);

        qryChats.addSnapshotListener((value, error) -> {
            chatArrayList.clear();
            for (DocumentSnapshot documentSnapshot : value.getDocuments()){
                Chat chat = documentSnapshot.toObject(Chat.class);
                chatArrayList.add(chat);
            }
            chatAdapter.notifyDataSetChanged();
            rvChat.smoothScrollToPosition(chatAdapter.getItemCount());
        });
    }

    @Override
    public void onChatClick(int position) {

    }
}