package com.example.mtaa;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import com.example.mtaa.databinding.FragmentMapBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.example.mtaa.models.Report;
import com.google.maps.android.clustering.Cluster;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener, GoogleMap.OnMarkerClickListener {
    private FragmentMapBinding binding;
    private GoogleMap mMap;
    private FirebaseFirestore db;
    private ClusterManager<Report> clusterManager;
    private TextView statusBarText;
    private View statusBar;
    private BottomSheetDialog locationInfoSheet;

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
        setupStatusBar();
        setupLocationInfoSheet();
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

    private void setupStatusBar() {
        statusBar = binding.getRoot().findViewById(R.id.map_status_bar);
        statusBarText = binding.getRoot().findViewById(R.id.status_bar_text);
        updateStatusBar("Map Ready");
    }

    private void setupLocationInfoSheet() {
        locationInfoSheet = new BottomSheetDialog(requireContext());
        View sheetView = getLayoutInflater().inflate(R.layout.map_location_info_sheet, null);
        locationInfoSheet.setContentView(sheetView);
    }

    private void updateStatusBar(String message) {
        if (statusBarText != null) {
            statusBarText.setText(message);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        if (getContext() == null) return;
        
        mMap = googleMap;
        try {
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
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error initializing map: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupClusterManager() {
        if (getContext() == null || mMap == null) return;
        
        try {
            clusterManager = new ClusterManager<>(requireContext(), mMap);
            clusterManager.setAnimation(true);
            
            // Set the cluster manager as the camera and marker click listener
            mMap.setOnCameraIdleListener(this);
            mMap.setOnMarkerClickListener(this);
            
            // Custom renderer for category-based markers and clusters
            clusterManager.setRenderer(new DefaultClusterRenderer<Report>(requireContext(), mMap, clusterManager) {
                @Override
                protected void onBeforeClusterItemRendered(@NonNull Report report, @NonNull MarkerOptions markerOptions) {
                    // Customize marker based on report category
                    BitmapDescriptor icon = getMarkerIconForCategory(report.getCategory());
                    markerOptions.icon(icon)
                              .title(report.getTitle())
                              .snippet(report.getDescription());
                }

                @Override
                protected void onBeforeClusterRendered(@NonNull Cluster<Report> cluster, @NonNull MarkerOptions markerOptions) {
                    // Customize cluster icon based on predominant category
                    markerOptions.icon(getClusterIcon(cluster));
                }
            });
            
            // Show detailed report info on marker click
            clusterManager.setOnClusterItemClickListener(report -> {
                if (getContext() != null) {
                    showReportDetails(report);
                }
                return true;
            });

            // Show cluster info on cluster click
            clusterManager.setOnClusterClickListener(cluster -> {
                showClusterSummary(cluster);
                return true;
            });
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error setting up map markers: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private BitmapDescriptor getMarkerIconForCategory(String category) {
        int resourceId;
        switch (category.toLowerCase()) {
            case "hazard":
                resourceId = R.drawable.ic_hazard_marker;
                break;
            case "incident":
                resourceId = R.drawable.ic_incident_marker;
                break;
            case "maintenance":
                resourceId = R.drawable.ic_maintenance_marker;
                break;
            default:
                resourceId = R.drawable.ic_default_marker;
        }
        return BitmapDescriptorFactory.fromResource(resourceId);
    }

    private BitmapDescriptor getClusterIcon(Cluster<Report> cluster) {
        int size = cluster.getSize();
        int backgroundColor = getClusterColor(size);
        
        // Create circular background with count
        Bitmap icon = Bitmap.createBitmap(120, 120, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(icon);
        Paint paint = new Paint();
        paint.setColor(backgroundColor);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(60, 60, 50, paint);
        
        // Draw count text
        paint.setColor(Color.WHITE);
        paint.setTextSize(40);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(String.valueOf(size), 60, 70, paint);
        
        return BitmapDescriptorFactory.fromBitmap(icon);
    }

    private int getClusterColor(int size) {
        if (size < 10) return Color.HSVToColor(new float[]{120, 1, 1}); // Green
        if (size < 50) return Color.HSVToColor(new float[]{60, 1, 1});  // Yellow
        if (size < 100) return Color.HSVToColor(new float[]{30, 1, 1}); // Orange
        return Color.HSVToColor(new float[]{0, 1, 1}); // Red
    }

    private void showReportDetails(Report report) {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(requireContext());
        View bottomSheetView = getLayoutInflater().inflate(R.layout.report_details_bottom_sheet, null);
        
        TextView titleView = bottomSheetView.findViewById(R.id.report_title);
        TextView descriptionView = bottomSheetView.findViewById(R.id.report_description);
        TextView categoryView = bottomSheetView.findViewById(R.id.report_category);
        TextView dateView = bottomSheetView.findViewById(R.id.report_date);
        ImageView categoryIcon = bottomSheetView.findViewById(R.id.category_icon);
        
        titleView.setText(report.getTitle());
        descriptionView.setText(report.getDescription());
        categoryView.setText(report.getCategory());
        dateView.setText(formatDate(report.getCreatedAt()));
        categoryIcon.setImageResource(getCategoryIconResource(report.getCategory()));
        
        bottomSheet.setContentView(bottomSheetView);
        bottomSheet.show();
    }

    private void showClusterSummary(Cluster<Report> cluster) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Cluster Summary");
        
        // Count reports by category
        Map<String, Integer> categoryCounts = new HashMap<>();
        for (Report report : cluster.getItems()) {
            String category = report.getCategory();
            categoryCounts.put(category, categoryCounts.getOrDefault(category, 0) + 1);
        }
        
        // Build summary text
        StringBuilder summary = new StringBuilder();
        for (Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
            summary.append(entry.getKey())
                   .append(": ")
                   .append(entry.getValue())
                   .append("\n");
        }
        
        builder.setMessage(summary.toString())
               .setPositiveButton("OK", null)
               .show();
    }

    private String formatDate(Date date) {
        if (date == null) return "";
        return new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(date);
    }

    private int getCategoryIconResource(String category) {
        switch (category.toLowerCase()) {
            case "hazard":
                return R.drawable.ic_hazard;
            case "incident":
                return R.drawable.ic_incident;
            case "maintenance":
                return R.drawable.ic_maintenance;
            default:
                return R.drawable.ic_default;
        }
    }

    @Override
    public void onCameraIdle() {
        if (clusterManager != null) {
            clusterManager.onCameraIdle();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return clusterManager != null && clusterManager.onMarkerClick(marker);
    }

    private void loadReportMarkers() {
        if (getContext() == null || mMap == null) {
            Log.e("MapFragment", "Context or map is null");
            return;
        }
        
        try {
            setupClusterManager();
            if (clusterManager == null) {
                Log.e("MapFragment", "Failed to setup cluster manager");
                return;
            }
            
            clusterManager.clearItems(); // Clear existing items before loading new ones
            
            db.collection("reports")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (getContext() == null || mMap == null || clusterManager == null) {
                            Log.e("MapFragment", "Context, map or cluster manager became null");
                            return;
                        }
                        
                        try {
                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                try {
                                    GeoPoint location = document.getGeoPoint("location");
                                    if (location == null) {
                                        Log.w("MapFragment", "Skipping report with null location: " + document.getId());
                                        continue;
                                    }
                                    
                                    Report report = document.toObject(Report.class);
                                    if (report == null) {
                                        Log.w("MapFragment", "Failed to parse report: " + document.getId());
                                        continue;
                                    }
                                    
                                    report.setId(document.getId()); // Ensure report has its ID
                                    report.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
                                    clusterManager.addItem(report);
                                } catch (Exception e) {
                                    Log.e("MapFragment", "Error processing report: " + document.getId(), e);
                                }
                            }
                            
                            clusterManager.cluster();
                            
                            if (!queryDocumentSnapshots.isEmpty()) {
                                GeoPoint firstLocation = queryDocumentSnapshots.getDocuments().get(0).getGeoPoint("location");
                                if (firstLocation != null) {
                                    LatLng defaultPosition = new LatLng(firstLocation.getLatitude(), firstLocation.getLongitude());
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultPosition, 12));
                                }
                            }
                        } catch (Exception e) {
                            Log.e("MapFragment", "Error processing map markers", e);
                            Toast.makeText(getContext(), "Error processing map markers: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("MapFragment", "Error loading reports", e);
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Error loading reports: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            Log.e("MapFragment", "Error in loadReportMarkers", e);
            Toast.makeText(getContext(), "Error loading map markers: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
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