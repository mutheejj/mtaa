package com.example.mtaa;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.UriPermission;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.mtaa.databinding.FragmentCreateReportBinding;
import com.example.mtaa.models.Report;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class CreateReportFragment extends Fragment implements OnMapReadyCallback {
    private FragmentCreateReportBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private FusedLocationProviderClient fusedLocationClient;
    private GoogleMap mMap;
    private Uri selectedImageUri;
    private LatLng selectedLocation;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<String> imagePickerLauncher;

    private final String[] categories = new String[]{
        "Road Damage", "Street Light", "Garbage", "Graffiti",
        "Traffic Signal", "Sidewalk", "Tree/Plant", "Other"
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        binding = FragmentCreateReportBinding.inflate(inflater, container, false);
        
        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        
        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        
        setupPermissionLaunchers();
        setupImagePicker();
        setupMapFragment();
        setupCategoryDropdown();
        setupClickListeners();
        
        return binding.getRoot();
    }

    private void setupPermissionLaunchers() {
        requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    getCurrentLocation();
                } else {
                    Toast.makeText(getContext(), "Location permission is required to detect your location",
                        Toast.LENGTH_LONG).show();
                }
            });
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    try {
                        ContentResolver contentResolver = requireContext().getContentResolver();
                        
                        // Take persistent permission
                        final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                            
                        contentResolver.takePersistableUriPermission(uri, takeFlags);
                        selectedImageUri = uri;
                        binding.imagePreview.setImageURI(uri);
                        binding.imagePreview.setVisibility(View.VISIBLE);
                    } catch (SecurityException e) {
                        String errorMessage = "Failed to access the image. Please try selecting a different image or check app permissions.";
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            });
    }

    private void setupMapFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
            .findFragmentById(R.id.mapPreview);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void setupCategoryDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            categories
        );
        ((AutoCompleteTextView) binding.categoryInput).setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> 
            Navigation.findNavController(v).navigateUp());

        binding.detectLocationButton.setOnClickListener(v -> checkLocationPermission());

        binding.addImageButton.setOnClickListener(v -> 
            imagePickerLauncher.launch("image/*"));

        binding.submitButton.setOnClickListener(v -> validateAndSubmitReport());
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationRequest locationRequest = LocationRequest.create()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(5000);

            LocationCallback locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        updateMapLocation(currentLocation);
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Location detected!", Toast.LENGTH_SHORT).show();
                        }
                        fusedLocationClient.removeLocationUpdates(this);
                    }
                }
            };

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error detecting location: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                });
        }
    }

    private void updateMapLocation(LatLng location) {
        if (mMap != null) {
            mMap.clear();
            mMap.addMarker(new MarkerOptions()
                .position(location)
                .title("Selected Location")
                .draggable(true));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f));
            selectedLocation = location;
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this::updateMapLocation);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        
        if (ContextCompat.checkSelfPermission(requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            getCurrentLocation();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void validateAndSubmitReport() {
        String title = binding.titleInput.getText().toString().trim();
        String description = binding.descriptionInput.getText().toString().trim();
        String category = binding.categoryInput.getText().toString().trim();

        if (title.isEmpty()) {
            binding.titleLayout.setError("Title is required");
            return;
        }

        if (description.isEmpty()) {
            binding.descriptionLayout.setError("Description is required");
            return;
        }

        if (category.isEmpty()) {
            binding.categoryLayout.setError("Category is required");
            return;
        }

        if (selectedLocation == null) {
            Toast.makeText(getContext(), "Please select a location", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.submitButton.setEnabled(false);
        uploadImageAndCreateReport(title, description, category);
    }

    private void uploadImageAndCreateReport(String title, String description, String category) {
        if (selectedImageUri != null) {
            try {
                // Validate image file existence and accessibility
                InputStream inputStream = getContext().getContentResolver().openInputStream(selectedImageUri);
                if (inputStream == null) {
                    Toast.makeText(getContext(), "Error: Cannot access the selected image", Toast.LENGTH_LONG).show();
                    binding.submitButton.setEnabled(true);
                    return;
                }
                inputStream.close();
                
                String imageFileName = "reports/" + UUID.randomUUID().toString() + ".jpg";
                StorageReference imageRef = storageRef.child(imageFileName);

                imageRef.putFile(selectedImageUri)
                    .addOnProgressListener(snapshot -> {
                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        binding.submitButton.setText("Uploading: " + (int)progress + "%");
                    })
                    .addOnSuccessListener(taskSnapshot -> {
                        binding.submitButton.setText("Submit Report");
                        Toast.makeText(getContext(), "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                        imageRef.getDownloadUrl().addOnSuccessListener(downloadUrl ->
                                createReport(title, description, category, downloadUrl.toString())
                        ).addOnFailureListener(e -> {
                            binding.submitButton.setEnabled(true);
                            Toast.makeText(getContext(), "Failed to get download URL: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
                    })
                    .addOnFailureListener(e -> {
                        binding.submitButton.setText("Retry Upload");
                        binding.submitButton.setEnabled(true);
                        Toast.makeText(getContext(), "Upload failed: " + e.getMessage() + "\nTap Submit to retry", Toast.LENGTH_LONG).show();
                    });
            } catch (IOException e) {
                binding.submitButton.setEnabled(true);
                Toast.makeText(getContext(), "Error accessing image file: " + e.getMessage(), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                binding.submitButton.setEnabled(true);
                Toast.makeText(getContext(), "Error accessing image file: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            createReport(title, description, category, null);
        }
    }

    private void createReport(String title, String description, String category, String imageUrl) {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(getContext(), "Please sign in to submit a report", Toast.LENGTH_LONG).show();
            return;
        }

        GeoPoint location = new GeoPoint(selectedLocation.latitude, selectedLocation.longitude);
        Report report = new Report(
            userId,
            title,
            description,
            category,
            location,
            imageUrl,
            com.google.firebase.Timestamp.now()
        );

        db.collection("reports").add(report)
            .addOnSuccessListener(documentReference -> {
                Toast.makeText(getContext(), "Report submitted successfully", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).navigateUp();
            })
            .addOnFailureListener(e -> {
                binding.submitButton.setEnabled(true);
                Toast.makeText(getContext(), "Failed to submit report: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            });
    }
}