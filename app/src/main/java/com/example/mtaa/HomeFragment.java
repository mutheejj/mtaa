package com.example.mtaa;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mtaa.adapters.ActivityAdapter;
import com.example.mtaa.adapters.AchievementAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.EditText;
import com.example.mtaa.models.Report;
import java.util.List;
import java.util.ArrayList;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.example.mtaa.models.Activity;
import com.example.mtaa.models.Achievement;
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

public class HomeFragment extends Fragment implements OnMapReadyCallback {
    private FragmentHomeBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private RecyclerView recentActivityRecyclerView;
    private RecyclerView achievementsRecyclerView;
    private ActivityAdapter activityAdapter;
    private AchievementAdapter achievementAdapter;

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
        
        // Initialize map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
            .findFragmentById(R.id.mapPreview);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        
        // Update user information
        updateUserInfo();
        
        // Initialize RecyclerViews
        setupRecyclerViews();
        
        // Load user stats
        loadUserStats();
        
        // Load recent activity
        loadRecentActivity();
        
        // Load achievements
        loadAchievements();

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
            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_settingsFragment)
        );

        binding.cardOfficial.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_officialFragment)
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
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            // Set email from FirebaseUser
            binding.userEmail.setText(currentUser.getEmail());
            
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
            .setMessage("Do you want to open the dialer with emergency services number?")
            .setPositiveButton("Open Dialer", (dialog, which) -> {
                try {
                    String emergencyNumber = "01000";
                    Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                    dialIntent.setData(Uri.parse("tel:" + emergencyNumber));
                    startActivity(dialIntent);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Failed to open phone dialer: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }

    private void setupRecyclerViews() {
        // Setup Recent Activity RecyclerView
        recentActivityRecyclerView = binding.recentActivityRecyclerView;
        recentActivityRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        activityAdapter = new ActivityAdapter();
        recentActivityRecyclerView.setAdapter(activityAdapter);

        // Setup Achievements RecyclerView
        achievementsRecyclerView = binding.achievementsRecyclerView;
        achievementsRecyclerView.setLayoutManager(
            new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        achievementAdapter = new AchievementAdapter();
        achievementsRecyclerView.setAdapter(achievementAdapter);
    }

    private void loadUserStats() {
        if (binding == null) return;
        
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            
            // Load reports count
            db.collection("reports")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(reports -> {
                    if (binding != null) {
                        binding.reportsCount.setText(String.valueOf(reports.size()));
                    }
                });

            // Load resolved issues count
            db.collection("reports")
                .whereEqualTo("userId", uid)
                .whereEqualTo("status", "resolved")
                .get()
                .addOnSuccessListener(resolved -> {
                    if (binding != null) {
                        binding.resolvedCount.setText(String.valueOf(resolved.size()));
                    }
                });

            // Calculate impact score (example: reports + (resolved * 2))
            db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(document -> {
                    if (binding != null && document.exists()) {
                        Long impactScore = document.getLong("impactScore");
                        binding.impactScore.setText(String.valueOf(impactScore != null ? impactScore : 0));
                    }
                });
        }
    }

    private void loadRecentActivity() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            db.collection("activities")
                .whereEqualTo("userId", currentUser.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener(activities -> {
                    List<Activity> activityList = new ArrayList<>();
                    for (DocumentSnapshot doc : activities) {
                        Activity activity = doc.toObject(Activity.class);
                        if (activity != null) {
                            activityList.add(activity);
                        }
                    }
                    activityAdapter.setActivities(activityList);
                });
        }
    }

    private void loadAchievements() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            db.collection("achievements")
                .whereEqualTo("userId", currentUser.getUid())
                .orderBy("unlockedAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(achievements -> {
                    List<Achievement> achievementList = new ArrayList<>();
                    for (DocumentSnapshot doc : achievements) {
                        Achievement achievement = doc.toObject(Achievement.class);
                        if (achievement != null) {
                            achievementList.add(achievement);
                        }
                    }
                    achievementAdapter.setAchievements(achievementList);
                });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        
        if (ContextCompat.checkSelfPermission(requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
    }
    private GoogleMap mMap;
}