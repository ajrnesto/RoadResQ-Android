package com.vulcanizingapp.Fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.vulcanizingapp.R;
import com.vulcanizingapp.Utils.Utils;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationSelectionMapFragment extends Fragment {

    FirebaseFirestore DB;
    FirebaseStorage STORAGE;
    FirebaseAuth AUTH;
    FirebaseUser USER;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        STORAGE = FirebaseStorage.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }

    View view;
    GoogleMap googleMap;
    Location currentLocation;

    MaterialButton btnGoToMyLocation, btnSelectLocation;

    FusedLocationProviderClient fusedLocationProviderClient;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(@NonNull GoogleMap map) {
            googleMap = map;
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style));
            getCurrentLocation();

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(new LatLng(9.063289672310495, 123.0380671989104))
                    .title("RoadResQ Shop, Poblacion 2, Siaton");

            Marker unitMarker = googleMap.addMarker(markerOptions);
            unitMarker.showInfoWindow();

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(9.063289672310495, 123.0380671989104), 18));

            btnSelectLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LatLng position = googleMap.getCameraPosition().target;
                    double latitude = position.latitude;
                    double longitude = position.longitude;

                    Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                        if (addresses.size() > 0) {
                            Address address = addresses.get(0);
                            String addressLine = address.getAddressLine(0);

                            Utils.Cache.setDouble(requireContext(), "selected_latitude", latitude);
                            Utils.Cache.setDouble(requireContext(), "selected_longitude", longitude);
                            Utils.Cache.setString(requireContext(), "addressLine", addressLine);

                            requireActivity().finish();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_location_selection_map, container, false);

        initialize();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }

        return view;
    }

    private void initialize() {
        btnGoToMyLocation = view.findViewById(R.id.btnGoToMyLocation);
        btnSelectLocation = view.findViewById(R.id.btnSelectLocation);
    }

    private void getCurrentLocation() {

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION }, 100);
            return;
        }

        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(60000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationCallback mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Toast.makeText(getContext()," location result is  " + locationResult, Toast.LENGTH_LONG).show();

                if (locationResult == null) {
                    Toast.makeText(getContext(),"current location is null ", Toast.LENGTH_LONG).show();

                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        Toast.makeText(getContext(),"current location is " + location.getLongitude(), Toast.LENGTH_LONG).show();

                        //TODO: UI updates.
                    }
                }
            }
        };

        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(location -> {
            if (location != null) {
                currentLocation = location;
                LatLng myLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                googleMap.setMyLocationEnabled(true);

                btnGoToMyLocation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 18));
                    }
                });
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            }
        }
    }
}