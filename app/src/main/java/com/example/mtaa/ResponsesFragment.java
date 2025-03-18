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
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class ResponsesFragment extends Fragment {
    private RecyclerView recyclerView;
    private List<Response> responseList;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private TabLayout tabLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ShimmerFrameLayout shimmerLayout;
    private TextView emptyView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_responses, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("Responses");

        initializeViews(view);
        setupSwipeRefresh();
        setupRecyclerView();
        setupTabLayout();

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        loadResponses(0); // Load comments by default
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.responses_recycler_view);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        shimmerLayout = view.findViewById(R.id.shimmer_layout);
        emptyView = view.findViewById(R.id.empty_view);
        tabLayout = view.findViewById(R.id.tab_layout);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> loadResponses(tabLayout.getSelectedTabPosition()));
        swipeRefreshLayout.setColorSchemeResources(R.color.text_primary);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        responseList = new ArrayList<>();
        ResponseAdapter adapter = new ResponseAdapter(responseList, false);
        recyclerView.setAdapter(adapter);
    }

    private void setupTabLayout() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                loadResponses(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadResponses(int tabPosition) {
        shimmerLayout.setVisibility(View.VISIBLE);
        shimmerLayout.startShimmer();
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);

        try {
            String userId = auth.getCurrentUser().getUid();
            final String collectionPath = tabPosition == 0 ? "comments" : tabPosition == 1 ? "updates" : "mentions";

            db.collection(collectionPath)
                    .whereEqualTo("userId", userId)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .addSnapshotListener((value, error) -> {
                        if (error != null) {
                            showError("Failed to load " + collectionPath);
                            updateViewsOnError();
                            return;
                        }

                        responseList.clear();
                        if (value != null) {
                            for (var doc : value) {
                                Response response = doc.toObject(Response.class);
                                response.setId(doc.getId());
                                responseList.add(response);
                            }
                        }

                        updateViewsOnSuccess();
                    });
        } catch (Exception e) {
            showError("Error loading responses");
            updateViewsOnError();
        }
    }

    private void updateViewsOnSuccess() {
        shimmerLayout.stopShimmer();
        shimmerLayout.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);

        if (responseList.isEmpty()) {
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
        emptyView.setText("Failed to load responses");
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
    }
