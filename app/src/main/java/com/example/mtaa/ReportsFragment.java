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
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class ReportsFragment extends Fragment {
    private RecyclerView recyclerView;
    private List<Report> reportList;
    private FirebaseFirestore db;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ShimmerFrameLayout shimmerLayout;
    private TextView emptyView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reports, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("Reports");

        initializeViews(view);
        setupRecyclerView();
        setupSwipeRefresh();

        db = FirebaseFirestore.getInstance();
        loadReports();
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.reports_recycler_view);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        shimmerLayout = view.findViewById(R.id.shimmer_layout);
        emptyView = view.findViewById(R.id.empty_view);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadReports);
        swipeRefreshLayout.setColorSchemeResources(R.color.text_primary);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        reportList = new ArrayList<>();
        ReportAdapter adapter = new ReportAdapter(reportList);
        recyclerView.setAdapter(adapter);
    }

    private void loadReports() {
        if (!isAdded() || getContext() == null) {
            return;
        }

        shimmerLayout.setVisibility(View.VISIBLE);
        shimmerLayout.startShimmer();
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);

        db.collection("reports")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (!isAdded()) return;

                    if (error != null) {
                        String errorMessage = "Failed to load reports";
                        if (error.getMessage() != null && error.getMessage().contains("PERMISSION_DENIED")) {
                            errorMessage = "You don't have permission to view reports";
                        }
                        showError(errorMessage);
                        updateViewsOnError(errorMessage);
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

                    updateViewsOnSuccess();
                });
    }

    private void updateViewsOnSuccess() {
        shimmerLayout.stopShimmer();
        shimmerLayout.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);

        if (reportList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    private void updateViewsOnError(String errorMessage) {
        if (!isAdded()) return;
        
        shimmerLayout.stopShimmer();
        shimmerLayout.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
        emptyView.setText(errorMessage);
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}