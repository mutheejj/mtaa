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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CreateReportFragment extends Fragment implements OnMapReadyCallback {
    private FragmentCreateReportBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocationClient;
    private GoogleSignInClient googleSignInClient;
    private Drive driveService;
    private static final int RC_SIGN_IN = 9001;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private GoogleMap mMap;
    private Uri selectedImageUri;
    private LatLng selectedLocation;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private Uri photoUri;

    private final String[] categories = new String[]{
        "Road Damage", "Street Light", "Garbage", "Graffiti",
        "Traffic Signal", "Sidewalk", "Tree/Plant", "Other"
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        binding = FragmentCreateReportBinding.inflate(inflater, container, false);
        
        // Initialize Firebase and Location Services
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        
        // Initialize Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
            .build();
        
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
        
        // Check for existing Google Sign In account
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(requireContext());
        if (account != null && account.getGrantedScopes().contains(new Scope(DriveScopes.DRIVE_FILE))) {
            setupDriveService(account);
        } else {
            startActivityForResult(googleSignInClient.getSignInIntent(), RC_SIGN_IN);
        }
        
        setupPermissionLaunchers();
        setupImagePicker();
        setupCameraCapture();
        setupMapFragment();
        setupCategoryDropdown();
        setupClickListeners();
        
        return binding.getRoot();
    }

    private void setupPermissionLaunchers() {
        cameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    launchCamera();
                } else {
                    Toast.makeText(getContext(), "Camera permission is required to take pictures",
                        Toast.LENGTH_LONG).show();
                }
            });

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

    private void setupCameraCapture() {
        takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (success && photoUri != null) {
                    selectedImageUri = photoUri;
                    binding.imagePreview.setImageURI(photoUri);
                    binding.imagePreview.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getContext(), "Failed to capture image", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(),
            Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchCamera() {
        try {
            String fileName = "photo_" + UUID.randomUUID().toString() + ".jpg";
            java.io.File photoFile = new java.io.File(requireContext().getCacheDir(), fileName);
            photoUri = androidx.core.content.FileProvider.getUriForFile(
                requireContext(),
                "com.example.mtaa.fileprovider",
                photoFile
            );
            takePictureLauncher.launch(photoUri);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error setting up camera: " + e.getMessage(),
                Toast.LENGTH_LONG).show();
        }
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    try {
                        ContentResolver contentResolver = requireContext().getContentResolver();
                        InputStream inputStream = contentResolver.openInputStream(uri);
                        if (inputStream == null) {
                            Toast.makeText(getContext(), "Error: Cannot access the selected image", Toast.LENGTH_LONG).show();
                            return;
                        }

                        // Create a temporary file in the app's cache directory
                        java.io.File cacheDir = requireContext().getCacheDir();
                        java.io.File tempFile = new java.io.File(cacheDir, "temp_image_" + UUID.randomUUID().toString() + ".jpg");
                        
                        // Copy the input stream to the temporary file
                        java.io.FileOutputStream outputStream = new java.io.FileOutputStream(tempFile);
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        outputStream.close();
                        inputStream.close();

                        // Update the selectedImageUri to point to the temporary file
                        selectedImageUri = Uri.fromFile(tempFile);
                        binding.imagePreview.setImageURI(selectedImageUri);
                        binding.imagePreview.setVisibility(View.VISIBLE);
                    } catch (IOException | SecurityException e) {
                        String errorMessage = "Failed to process the selected image: " + e.getMessage();
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

        binding.takePictureButton.setOnClickListener(v -> checkCameraPermission());

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignIn.getSignedInAccountFromIntent(data)
                .addOnSuccessListener(account -> setupDriveService(account))
                .addOnFailureListener(e -> {
                    binding.submitButton.setEnabled(true);
                    Toast.makeText(getContext(), "Google Sign In failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
        }
    }

    private void setupDriveService(GoogleSignInAccount account) {
        if (account == null) {
            Toast.makeText(requireContext(), "Google Sign In required", Toast.LENGTH_LONG).show();
            binding.submitButton.setEnabled(true);
            return;
        }

        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
            requireContext(),
            Collections.singleton(DriveScopes.DRIVE_FILE)
        );
        credential.setSelectedAccount(account.getAccount());

        driveService = new Drive.Builder(
            AndroidHttp.newCompatibleTransport(),
            new GsonFactory(),
            credential)
            .setApplicationName("MTAA App")
            .build();
    }

    private void uploadImageAndCreateReport(String title, String description, String category) {
        if (driveService == null) {
            Toast.makeText(requireContext(), "Google Drive service not initialized. Please try again.", Toast.LENGTH_LONG).show();
            binding.submitButton.setEnabled(true);
            return;
        }

        if (selectedImageUri != null) {
            try {
                ContentResolver contentResolver = requireContext().getContentResolver();
                InputStream inputStream = contentResolver.openInputStream(selectedImageUri);
                if (inputStream == null) {
                    Toast.makeText(getContext(), "Error: Cannot access the selected image", Toast.LENGTH_LONG).show();
                    binding.submitButton.setEnabled(true);
                    return;
                }

                executor.execute(() -> {
                    try {
                        // Create file metadata
                        File fileMetadata = new File();
                        fileMetadata.setName(UUID.randomUUID().toString() + ".jpg");
                        fileMetadata.setMimeType("image/jpeg");

                        // Upload file to Drive
                        File uploadedFile = driveService.files().create(fileMetadata, 
                            new com.google.api.client.http.InputStreamContent("image/jpeg", inputStream))
                            .setFields("id, webViewLink")
                            .execute();

                        // Get the shareable link
                        String imageUrl = uploadedFile.getWebViewLink();
                        
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                            createReport(title, description, category, imageUrl);
                        });
                    } catch (IOException e) {
                        requireActivity().runOnUiThread(() -> {
                            binding.submitButton.setEnabled(true);
                            Toast.makeText(getContext(), "Failed to upload image: " + e.getMessage(), 
                                Toast.LENGTH_LONG).show();
                        });
                    } finally {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
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