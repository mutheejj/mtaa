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
import com.example.mtaa.databinding.FragmentHomeBinding;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.mtaa.models.Post;
import com.google.firebase.Timestamp;
import com.example.mtaa.adapters.PostAdapter;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        // Update user information
        updateUserInfo();

        // Set click listeners for cards
        binding.cardReportIssue.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_createReportFragment)
        );

        binding.cardViewMap.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_mapFragment)
        );

        binding.cardMyReports.setOnClickListener(v -> {
            // Navigate to filtered reports showing only user's reports
            Bundle args = new Bundle();
            args.putBoolean("showUserReportsOnly", true);
            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_reportsFragment, args);
        });

        binding.cardTrending.setOnClickListener(v -> {
            // Navigate to reports sorted by popularity/urgency
            Bundle args = new Bundle();
            args.putString("sortBy", "trending");
            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_reportsFragment, args);
        });

        binding.cardResponses.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_responsesFragment)
        );

        binding.cardSettings.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_profileFragment)
        );

        binding.fabReport.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_createReportFragment)
        );

        binding.notificationBell.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_notificationsFragment)
        );

        // Set up emergency alert card
        binding.emergencyCard.setOnClickListener(v -> {
            // Launch emergency contact dialog or screen
            showEmergencyDialog();
        });
    }

    private void updateUserInfo() {
        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            db.collection("users").document(uid).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String name = document.getString("name");
                        String profileImageUrl = document.getString("profileImage");
                        
                        // Update user name
                        binding.userName.setText(name);
                        
                        // Load profile image
                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Glide.with(this)
                                .load(profileImageUrl)
                                .placeholder(R.drawable.default_avatar)
                                .error(R.drawable.default_avatar)
                                .into(binding.profileImage);
                        }
                    }
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(getContext(), "Error loading user data: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show()
                );
        }
    }

    private void showEmergencyDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Emergency Contact")
            .setMessage("Do you want to contact emergency services?")
            .setPositiveButton("Call Emergency", (dialog, which) -> {
                // Implement emergency call functionality
                Toast.makeText(getContext(), "Connecting to emergency services...",
                    Toast.LENGTH_LONG).show();
            })
            .setNegativeButton("Cancel", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}