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

import com.example.mtaa.Response;
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_responses, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("Responses");

        recyclerView = view.findViewById(R.id.responses_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        responseList = new ArrayList<>();
        ResponseAdapter adapter = new ResponseAdapter(responseList);
        recyclerView.setAdapter(adapter);

        tabLayout = view.findViewById(R.id.tab_layout);
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

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        loadResponses(0); // Load comments by default
    }

    private void loadResponses(int tabPosition) {
        String userId = auth.getCurrentUser().getUid();
        String collectionPath = "";
        
        switch (tabPosition) {
            case 0: // Comments
                collectionPath = "comments";
                break;
            case 1: // Updates
                collectionPath = "updates";
                break;
            case 2: // Mentions
                collectionPath = "mentions";
                break;
        }

        db.collection(collectionPath)
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
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
                    recyclerView.getAdapter().notifyDataSetChanged();
                });
    }
}