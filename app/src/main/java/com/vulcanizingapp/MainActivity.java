package com.vulcanizingapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.vulcanizingapp.Fragments.FormAppointmentFragment;
import com.vulcanizingapp.Fragments.AppointmentsFragment;
import com.vulcanizingapp.Fragments.ProfileFragment;
import com.vulcanizingapp.Fragments.ServicesFragment;
import com.vulcanizingapp.Fragments.ShopFragment;
import com.vulcanizingapp.Fragments.WaitRescueFragment;
import com.vulcanizingapp.Utils.Utils;

import java.util.Arrays;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    FirebaseFirestore DB;
    FirebaseAuth AUTH;
    FirebaseUser USER;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }

    BottomNavigationView bottom_navbar;
    TextView tvActivityTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeFirebase();
        initializeViews();
        handleUserInteraction();
        softKeyboardListener();
        if (USER != null) {
            listenForOrderNotifications();
            listenForAppointmentNotifications();
        }
        backstackListener();

        // load up default fragment
        tvActivityTitle.setText("RoadResQ");
        bottom_navbar.findViewById(R.id.miShop).performClick();

        // check if user has an active rescue request
        if (USER != null) {
            DB.collection("rescue").document(USER.getUid())
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot snap, @Nullable FirebaseFirestoreException e) {
                            if (!Objects.equals(snap.getString("status"), "COMPLETE")) {
                                FragmentManager fragmentManager = getSupportFragmentManager();
                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                Fragment waitRescueFragment = new WaitRescueFragment();
                                fragmentTransaction.replace(R.id.fragmentHolder, waitRescueFragment, "WAIT_RESCUE_FRAGMENT");
                                fragmentTransaction.addToBackStack("WAIT_RESCUE_FRAGMENT");
                                fragmentTransaction.commit();
                            }
                        }
                    });
        }
    }

    @Override
    public void onBackPressed() {
        int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
        if (backStackEntryCount == 1) { // if navigation is at first backstack entry
            finish();
        } else {
            super.onBackPressed();
        }
    }

    private void initializeViews() {
        tvActivityTitle = findViewById(R.id.tvActivityTitle);
        bottom_navbar = findViewById(R.id.bottom_navbar);
    }

    private void handleUserInteraction() {
        bottom_navbar.setOnItemSelectedListener(item -> {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            switch (item.getItemId()) {
                case R.id.miServices:
                    Utils.hideKeyboard(this);
                    if (USER == null){
                        Utils.loginRequiredDialog(MainActivity.this, bottom_navbar, "You need to be logged in to book appointments");
                        return false;
                    }
                    tvActivityTitle.setText("Service Appointments");
                    if (bottom_navbar.getSelectedItemId() != R.id.miServices) {
                        Fragment servicesFragment = new ServicesFragment();
                        fragmentTransaction.replace(R.id.fragmentHolder, servicesFragment, "SERVICES_FRAGMENT");
                        fragmentTransaction.addToBackStack("SERVICES_FRAGMENT");
                        fragmentTransaction.commit();
                    }
                    break;
                case R.id.miShop:
                    tvActivityTitle.setText("RoadResQ Shop");
                    Utils.hideKeyboard(this);
                    if (bottom_navbar.getSelectedItemId() != R.id.miShop) {
                        Fragment shopFragment = new ShopFragment();
                        fragmentTransaction.replace(R.id.fragmentHolder, shopFragment, "SHOP_FRAGMENT");
                        fragmentTransaction.addToBackStack("SHOP_FRAGMENT");
                        fragmentTransaction.commit();
                    }
                    break;
                case R.id.miProfile:
                    Utils.hideKeyboard(this);
                    if (USER == null){
                        Utils.loginRequiredDialog(MainActivity.this, bottom_navbar, "You need to be logged in to manage your account");
                        return false;
                    }
                    if (bottom_navbar.getSelectedItemId() != R.id.miProfile) {
                        Fragment profileFragment = new ProfileFragment();
                        fragmentTransaction.replace(R.id.fragmentHolder, profileFragment, "PROFILE_FRAGMENT");
                        fragmentTransaction.addToBackStack("PROFILE_FRAGMENT");
                        fragmentTransaction.commit();
                    }
                    break;
            }
            return true;
        });
    }

    private void backstackListener() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.addOnBackStackChangedListener(() -> {
            ProfileFragment profileFragment = (ProfileFragment) getSupportFragmentManager().findFragmentByTag("PROFILE_FRAGMENT");
            ServicesFragment servicesFragment = (ServicesFragment) getSupportFragmentManager().findFragmentByTag("SERVICES_FRAGMENT");
            AppointmentsFragment appointmentsFragment = (AppointmentsFragment) getSupportFragmentManager().findFragmentByTag("APPOINTMENTS_FRAGMENT");
            FormAppointmentFragment formAppointmentFragment = (FormAppointmentFragment) getSupportFragmentManager().findFragmentByTag("FORM_APPOINTMENTS_FRAGMENT");
            ShopFragment shopFragment = (ShopFragment) getSupportFragmentManager().findFragmentByTag("SHOP_FRAGMENT");
            WaitRescueFragment waitRescueFragment = (WaitRescueFragment) getSupportFragmentManager().findFragmentByTag("WAIT_RESCUE_FRAGMENT");

            if (profileFragment != null && profileFragment.isVisible()) {
                tvActivityTitle.setText("Profile");
            }
            else if (servicesFragment != null && servicesFragment.isVisible()) {
                tvActivityTitle.setText("RoadResQ Services");
                Utils.Cache.setBoolean(getApplicationContext(), "sos_mode", false);
            }
            else if (appointmentsFragment != null && appointmentsFragment.isVisible()) {
                tvActivityTitle.setText("Your Service Appointments");
            }
            else if (formAppointmentFragment != null && formAppointmentFragment.isVisible()) {
                if (!Utils.Cache.getBoolean(getApplicationContext(), "sos_mode")) {
                    tvActivityTitle.setText("Book an Appointment");
                }
                else {
                    tvActivityTitle.setText("Get Rescued");
                }
            }
            else if (shopFragment != null && shopFragment.isVisible()) {
                tvActivityTitle.setText("RoadResQ Shop");
            }
            else if (waitRescueFragment != null && waitRescueFragment.isVisible()) {
                tvActivityTitle.setText("Get Rescued");
            }
        });
    }

    private void listenForOrderNotifications() {
        Query qryReadyRequests = DB.collection("orders")
                .whereEqualTo("customer", Objects.requireNonNull(AUTH.getCurrentUser()).getUid())
                .whereIn("status", Arrays.asList("Ready for Pick-up", "Completed"));
        qryReadyRequests.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                //If something went wrong
                if (e != null)
                    Log.w("TAG", "ERROR : ", e);

                if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                    //Instead of simply using the entire query snapshot
                    //See the actual changes to query results between query snapshots (added, removed, and modified)
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        if (!documentSnapshot.contains("wasNotified")) {
                            buildOrdersNotification(documentSnapshot.getId(), documentSnapshot.getString("status"));
                        }
                    }
                }
            }
        });
    }

    private void buildOrdersNotification(String id, String status) {
        DB.collection("orders").document(id)
                .update("wasNotified", true);
        String notificationMessage = "";
        if (Objects.equals(status, "Ready for Pick-up")) {
            notificationMessage = "Your order is now ready for pick up";
        }
        else if (Objects.equals(status, "Completed")) {
            notificationMessage = "Your order has been completed";
        }
        String channelID = "Orders Notifications";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelID);
        builder.setContentTitle("RoadResQ")
                .setSmallIcon(R.mipmap.ic_launcher_roadresq)
                .setContentText(notificationMessage)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX);

        Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_MUTABLE);
        builder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = notificationManager.getNotificationChannel(channelID);

            if (notificationChannel == null) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                notificationChannel = new NotificationChannel(channelID, "Orders Notifications", importance);
                notificationChannel.enableVibration(true);
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        notificationManager.notify(0, builder.build());
    }

    private void listenForAppointmentNotifications() {
        Query qryReadyRequests = DB.collection("appointments")
                .whereEqualTo("userUid", Objects.requireNonNull(AUTH.getCurrentUser()).getUid())
                .whereIn("status", Arrays.asList("IN SERVICE", "COMPLETED"))
                .orderBy("timestamp", Query.Direction.DESCENDING);
        qryReadyRequests.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                //If something went wrong
                if (e != null)
                    Log.w("TAG", "ERROR : ", e);

                if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                    //Instead of simply using the entire query snapshot
                    //See the actual changes to query results between query snapshots (added, removed, and modified)
                    for (DocumentChange docChange : queryDocumentSnapshots.getDocumentChanges()) {
                        if (docChange.getType() == DocumentChange.Type.ADDED) {
                            buildAppointmentsNotification(docChange.getDocument().getString("status"));
                        }
                    }
                }
            }
        });
    }

    private void buildAppointmentsNotification(String status) {
        String notificationMessage = "";
        if (Objects.equals(status, "IN SERVICE")) {
            notificationMessage = "Your appointment is now in service";
        }
        else if (Objects.equals(status, "COMPLETED")) {
            notificationMessage = "Your appointment is now completed";
        }
        
        String channelID = "Appointments Notifications";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelID);
        builder.setContentTitle("RoadResQ")
                .setSmallIcon(R.mipmap.ic_launcher_roadresq)
                .setContentText(notificationMessage)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX);

        Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_MUTABLE);
        builder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = notificationManager.getNotificationChannel(channelID);

            if (notificationChannel == null) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                notificationChannel = new NotificationChannel(channelID, "Appointments Notifications", importance);
                notificationChannel.enableVibration(true);
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        notificationManager.notify(1, builder.build());
    }

    private void softKeyboardListener() {
        getWindow().getDecorView().setOnApplyWindowInsetsListener((view, windowInsets) -> {
            WindowInsetsCompat insetsCompat = WindowInsetsCompat.toWindowInsetsCompat(windowInsets, view);
            if (insetsCompat.isVisible(WindowInsetsCompat.Type.ime())) {
                bottom_navbar.setVisibility(View.GONE);
            }
            else {
                bottom_navbar.setVisibility(View.VISIBLE);
            }
            return windowInsets;
        });
    }
}