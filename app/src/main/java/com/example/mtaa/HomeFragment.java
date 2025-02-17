package com.example.mtaa;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.EditText;
import com.example.mtaa.models.Report;
import java.util.List;
import java.util.ArrayList;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.navigation.Navigation;
import androidx.core.content.ContextCompat;

public class HomeFragment extends Fragment {
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private RecyclerView reportsRecyclerView;
    private EditText searchInput;
    private List<Report> reports = new ArrayList<>();
    private BottomNavigationView bottomNavigationView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        
        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Setup bottom navigation
        bottomNavigationView = view.findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                return true;
            } else if (itemId == R.id.navigation_reports) {
                // Navigate to reports
                return true;
            } else if (itemId == R.id.navigation_profile) {
                // Navigate to profile
                return true;
            }
            return false;
        });

        // Setup map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> {
                mMap = googleMap;
                // Enable my location if permission is granted
                enableMyLocation();
            });
        }

        // Setup FAB
        FloatingActionButton fab = view.findViewById(R.id.fab_create_report);
        fab.setOnClickListener(v -> {
            // Navigate to create report
            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_createReportFragment);
        });

        return view;
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
    }

    private void loadReports() {
        db.collection("reports")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    mMap.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Report report = document.toObject(Report.class);
                        if (report != null && report.getLocation() != null) {
                            LatLng position = new LatLng(report.getLocation().getLatitude(),
                                                       report.getLocation().getLongitude());
                            mMap.addMarker(new MarkerOptions()
                                    .position(position)
                                    .title(report.getTitle())
                                    .snippet(report.getDescription()));
                        }
                    }
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(getContext(), "Error loading reports: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(getContext(), "Location permission is required",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
} 