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

import com.example.mtaa.models.Report;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TrendingFragment extends Fragment {
    private RecyclerView recyclerView;
    private List<Report> reportList;
    private FirebaseFirestore db;
    private TabLayout tabLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ShimmerFrameLayout shimmerLayout;
    private TextView emptyView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trending, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("Trending Reports");

        initializeViews(view);
        setupSwipeRefresh();
        setupRecyclerView();
        setupTabLayout();

        db = FirebaseFirestore.getInstance();
        loadTrendingReports(0); // Load today's trending reports by default
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.trending_recycler_view);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        shimmerLayout = view.findViewById(R.id.shimmer_layout);
        emptyView = view.findViewById(R.id.empty_view);
        tabLayout = view.findViewById(R.id.tab_layout);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> loadTrendingReports(tabLayout.getSelectedTabPosition()));
        swipeRefreshLayout.setColorSchemeResources(R.color.text_primary);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        reportList = new ArrayList<>();
        ReportAdapter adapter = new ReportAdapter(reportList, false);
        recyclerView.setAdapter(adapter);
    }

    private void setupTabLayout() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                loadTrendingReports(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadTrendingReports(int tabPosition) {
        shimmerLayout.setVisibility(View.VISIBLE);
        shimmerLayout.startShimmer();
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        switch (tabPosition) {
            case 1: // This Week
                calendar.add(Calendar.DAY_OF_YEAR, -7);
                break;
            case 2: // This Month
                calendar.add(Calendar.MONTH, -1);
                break;
            default: // Today
                break;
        }

        long startTime = calendar.getTimeInMillis();

        db.collection("reports")
                .whereGreaterThan("timestamp", startTime)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        showError("Failed to load trending reports");
                        updateViewsOnError();
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

    private void updateViewsOnError() {
        shimmerLayout.stopShimmer();
        shimmerLayout.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
        emptyView.setText("Failed to load trending reports");
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
    }
