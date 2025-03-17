package com.example.mtaa;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trending, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("Trending Reports");

        recyclerView = view.findViewById(R.id.trending_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        reportList = new ArrayList<>();
        ReportAdapter adapter = new ReportAdapter(reportList);
        recyclerView.setAdapter(adapter);

        tabLayout = view.findViewById(R.id.tab_layout);
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

        db = FirebaseFirestore.getInstance();
        loadTrendingReports(0); // Load today's trending reports by default
    }

    private void loadTrendingReports(int tabPosition) {
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
                    recyclerView.getAdapter().notifyDataSetChanged();
                });
    }
}