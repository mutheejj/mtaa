package com.example.mtaa;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import com.example.mtaa.databinding.FragmentMapBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.example.mtaa.models.Report;
import java.util.Objects;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    private FragmentMapBinding binding;
    private GoogleMap mMap;
    private FirebaseFirestore db;
    private ClusterManager<Report> clusterManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeMap();
        setupToolbar();
    }

    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(requireContext(), "Error: Could not initialize map", Toast.LENGTH_LONG).show();
        }
    }

    private void setupToolbar() {
        binding.toolbar.setTitle(R.string.map_view);
        binding.toolbar.setNavigationOnClickListener(v -> 
            requireActivity().onBackPressed());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        // Set default position to a central location (e.g., Bratislava)
        LatLng defaultLocation = new LatLng(48.1486, 17.1077);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10));

        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            loadReportMarkers();
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    private void setupClusterManager() {
        clusterManager = new ClusterManager<>(requireContext(), mMap);
        mMap.setOnCameraIdleListener(clusterManager);
        mMap.setOnMarkerClickListener(clusterManager);
        
        clusterManager.setRenderer(new DefaultClusterRenderer<>(requireContext(), mMap, clusterManager));
        clusterManager.setOnClusterItemInfoWindowClickListener(report -> {
            // Handle marker click - you can navigate to report details here
            Toast.makeText(requireContext(), "Report: " + report.getTitle(), Toast.LENGTH_SHORT).show();
        });
    }

    private void loadReportMarkers() {
        setupClusterManager();
        
        db.collection("reports")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    queryDocumentSnapshots.forEach(document -> {
                        GeoPoint location = document.getGeoPoint("location");
                        if (location != null) {
                            Report report = document.toObject(Report.class);
                            report.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
                            clusterManager.addItem(report);
                        }
                    });
                    
                    clusterManager.cluster();
                    
                    if (!queryDocumentSnapshots.isEmpty()) {
                        GeoPoint firstLocation = queryDocumentSnapshots.getDocuments().get(0).getGeoPoint("location");
                        if (firstLocation != null) {
                            LatLng defaultPosition = new LatLng(firstLocation.getLatitude(), firstLocation.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultPosition, 12));
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(),
                        "Error loading reports: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 1 && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
                loadReportMarkers();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}