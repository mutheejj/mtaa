package com.example.mtaa;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class AdminDashboardFragment extends Fragment {
    private RecyclerView reportsRecyclerView;
    private List<Report> reportList;
    private FirebaseFirestore db;
    private TextView totalReportsCount;
    private TextView pendingReportsCount;
    private TextView resolvedReportsCount;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ShimmerFrameLayout shimmerLayout;
    private ReportAdapter reportAdapter;
    private boolean isDataLoading = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("Admin Dashboard");

        initializeViews(view);
        setupRecyclerView();
        loadDashboardData();

        // Setup quick action cards
        setupQuickActionCards(view);
    }

    private void initializeViews(View view) {
        reportsRecyclerView = view.findViewById(R.id.reports_recycler_view);
        totalReportsCount = view.findViewById(R.id.total_reports_count);
        pendingReportsCount = view.findViewById(R.id.pending_reports_count);
        resolvedReportsCount = view.findViewById(R.id.resolved_reports_count);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        shimmerLayout = view.findViewById(R.id.shimmer_layout);

        setupFirestore();
        setupSwipeRefresh();
    }

    private void setupFirestore() {
        if (!checkGooglePlayServices()) {
            showError("Google Play Services not available");
            return;
        }

        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
    }

    private boolean checkGooglePlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(requireContext());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(requireActivity(), resultCode, 9000).show();
            }
            return false;
        }
        return true;
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadDashboardData);
        swipeRefreshLayout.setColorSchemeResources(R.color.text_primary);
    }

    private void setupRecyclerView() {
        if (getContext() == null || reportsRecyclerView == null) return;
        
        reportsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        reportList = new ArrayList<>();
        reportAdapter = new ReportAdapter(reportList);
        reportsRecyclerView.setAdapter(reportAdapter);
    }

    private void setupQuickActionCards(View view) {
        MaterialCardView manageUsersCard = view.findViewById(R.id.manage_users_card);
        MaterialCardView systemSettingsCard = view.findViewById(R.id.system_settings_card);
        MaterialCardView analyticsCard = view.findViewById(R.id.analytics_card);

        manageUsersCard.setOnClickListener(v -> {
            // Navigate to user management
            // Navigation.findNavController(v).navigate(R.id.action_adminDashboardFragment_to_userManagementFragment);
        });

        systemSettingsCard.setOnClickListener(v -> {
            // Navigate to system settings
            // Navigation.findNavController(v).navigate(R.id.action_adminDashboardFragment_to_systemSettingsFragment);
        });

        analyticsCard.setOnClickListener(v -> {
            // Navigate to analytics
            // Navigation.findNavController(v).navigate(R.id.action_adminDashboardFragment_to_analyticsFragment);
        });
    }

    private void loadDashboardData() {
        if (!isAdded() || getContext() == null || isDataLoading) return;
        if (!checkGooglePlayServices()) return;
        
        isDataLoading = true;

        shimmerLayout.setVisibility(View.VISIBLE);
        shimmerLayout.startShimmer();
        reportsRecyclerView.setVisibility(View.GONE);
        // Load total reports count
        db.collection("reports").get()
            .addOnSuccessListener(querySnapshot -> {
                if (!isAdded()) return;
                totalReportsCount.setText(String.valueOf(querySnapshot.size()));
            })
            .addOnFailureListener(e -> {
                if (!isAdded()) return;
                showError("Failed to load total reports: " + e.getMessage());
            });

        // Load pending reports count
        db.collection("reports")
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    pendingReportsCount.setText(String.valueOf(querySnapshot.size()));
                })
                .addOnFailureListener(e -> showError("Failed to load pending reports"));

        // Load resolved reports count
        db.collection("reports")
                .whereEqualTo("status", "resolved")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    resolvedReportsCount.setText(String.valueOf(querySnapshot.size()));
                })
                .addOnFailureListener(e -> showError("Failed to load resolved reports"));

        // Load recent reports
        db.collection("reports")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(5)
                .addSnapshotListener((value, error) -> {
                    if (!isAdded()) return;
                    
                    shimmerLayout.stopShimmer();
                    shimmerLayout.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);

                    if (error != null) {
                        String errorMessage = "Failed to load recent reports";
                        if (error.getMessage() != null && error.getMessage().contains("PERMISSION_DENIED")) {
                            errorMessage = "You don't have permission to view reports";
                        }
                        showError(errorMessage);
                        return;
                    }

                    reportList.clear();
                    if (value != null) {
                        for (var doc : value) {
                            Report report = doc.toObject(Report.class);
                            report.setId(doc.getId());
                            reportList.add(report);
                        }
                    }

                    reportsRecyclerView.setVisibility(View.VISIBLE);
                    if (reportAdapter != null) {
                        reportAdapter.notifyDataSetChanged();
                    }
                    isDataLoading = false;
                });
    }

    private void showError(String message) {
        if (getContext() != null && isAdded()) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
        isDataLoading = false;
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
        shimmerLayout.stopShimmer();
        shimmerLayout.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);
    }
    }
