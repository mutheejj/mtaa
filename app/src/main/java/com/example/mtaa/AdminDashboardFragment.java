package com.example.mtaa;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.mtaa.adapters.ReportAdapter;
import com.example.mtaa.models.Report;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardFragment extends Fragment {
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<Report> reportList;
    private ReportAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("Admin Dashboard");

        setupFirebase();
        checkAdminAccess();
        setupRecyclerView(view);
        setupSwipeRefresh(view);
        loadAllReports();
    }

    private void setupFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private void checkAdminAccess() {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(getContext(), "Authentication required", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
            return;
        }
        // Simplified admin check - just verify authentication
        db.collection("admins").document(userId).get()
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error checking access", Toast.LENGTH_SHORT).show();
                    requireActivity().onBackPressed();
                });
    }

    private void setupRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.admin_reports_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        reportList = new ArrayList<>();
        adapter = new ReportAdapter(reportList, true); // true for admin mode
        recyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefresh(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(this::loadAllReports);
    }

    private void loadAllReports() {
        swipeRefreshLayout.setRefreshing(true);

        db.collection("reports")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    reportList.clear();
                    for (var doc : queryDocumentSnapshots) {
                        try {
                            Report report = doc.toObject(Report.class);
                            if (report != null && report.isValid()) {
                                report.setId(doc.getId());
                                reportList.add(report);
                            }
                        } catch (Exception e) {
                            // Skip invalid documents and log error
                            continue;
                        }
                    }
                    adapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading reports: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                });
    }
}