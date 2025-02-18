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

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        
        // Update user name from Firebase Auth
        updateUserName();

        // Set click listeners for cards
        binding.cardReportIssue.setOnClickListener(v -> {
            // Navigation will be implemented later
        });

        binding.cardViewMap.setOnClickListener(v -> {
            // Navigation will be implemented later
        });

        binding.cardMyReports.setOnClickListener(v -> {
            // Navigation will be implemented later
        });

        binding.cardTrending.setOnClickListener(v -> {
            // Navigation will be implemented later
        });

        binding.cardResponses.setOnClickListener(v -> {
            // Navigation will be implemented later
        });

        binding.cardSettings.setOnClickListener(v -> {
            // Navigation will be implemented later
        });

        binding.fabReport.setOnClickListener(v -> {
            // Quick report action will be implemented later
        });

        binding.notificationBell.setOnClickListener(v -> {
            // Notification handling will be implemented later
        });
    }

    private void updateUserName() {
        if (mAuth.getCurrentUser() != null) {
            String displayName = mAuth.getCurrentUser().getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                binding.userName.setText(displayName);
            } else {
                // Fallback to email if name is not set
                String email = mAuth.getCurrentUser().getEmail();
                if (email != null) {
                    // Remove everything after @ in email
                    String username = email.split("@")[0];
                    // Capitalize first letter
                    username = username.substring(0, 1).toUpperCase() + username.substring(1);
                    binding.userName.setText(username);
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 