package com.example.mtaa;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class Report implements ClusterItem {
    private String id;
    private String title;
    private String description;
    private String category;
    private String userId;
    private long timestamp;
    private transient LatLng position; // transient to handle Firestore serialization

    public Report() {
        // Required empty constructor for Firestore
    }

    public Report(String title, String description, String category, String userId, LatLng position) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.userId = userId;
        this.position = position;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return description;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    @Override
    public Float getZIndex() {
        return 1.0f;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}