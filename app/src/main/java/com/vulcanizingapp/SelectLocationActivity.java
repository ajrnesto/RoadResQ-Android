package com.vulcanizingapp;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.vulcanizingapp.Fragments.LocationSelectionMapFragment;

public class SelectLocationActivity extends AppCompatActivity {

    BottomNavigationView bottom_navbar;

    // top action bar elements
    MaterialButton btnBack;
    TextView tvActivityTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_location);

        initialize();
        initializeTopActionBar();

        Fragment locationSelectionMapFragment = new LocationSelectionMapFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction= fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, locationSelectionMapFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void initialize() {
        bottom_navbar = findViewById(R.id.bottom_navbar);
    }

    private void initializeTopActionBar() {
        tvActivityTitle = findViewById(R.id.tvActivityTitle);
        btnBack = findViewById(R.id.btnActionBar);

        tvActivityTitle.setText("Select Location");
        btnBack.setOnClickListener(view -> {
            onBackPressed();
        });
    }
}