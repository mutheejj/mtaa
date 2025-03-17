package com.example.mtaa;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
import java.util.ArrayList;
import java.util.List;

public class Report implements ClusterItem {
    private String id;
    private String title;
    private String description;
    private String category;
    private String userId;
    private long timestamp;
    private String status = "Pending"; // Default status
    private transient LatLng position; // transient to handle Firestore serialization
    private double latitude;
    private double longitude;
    private String snippet;
    private List<String> comments = new ArrayList<>();
    private int upvotes = 0;

    @com.google.firebase.firestore.Exclude
    private Long commentsCount; // Temporary field to handle legacy data

    public void setCommentsCount(Long count) {
        this.commentsCount = count;
        if (this.comments == null) {
            this.comments = new ArrayList<>();
        }
        // Convert legacy Long comments count to empty comment list
        if (count != null && (this.comments == null || this.comments.isEmpty())) {
            this.comments = new ArrayList<>();
        }
    }

    @com.google.firebase.firestore.PropertyName("comments")
    public void setCommentsFromFirestore(Object commentsObj) {
        if (commentsObj instanceof List) {
            this.comments = (List<String>) commentsObj;
        } else if (commentsObj instanceof Long) {
            this.commentsCount = (Long) commentsObj;
            this.comments = new ArrayList<>();
        } else {
            this.comments = new ArrayList<>();
        }
    }

    public Report() {
        // Required empty constructor for Firestore
    }

    public Report(String title, String description, String category, String userId, LatLng position) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.userId = userId;
        this.position = position;
        this.latitude = position.latitude;
        this.longitude = position.longitude;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public LatLng getPosition() {
        if (position == null && latitude != 0 && longitude != 0) {
            position = new LatLng(latitude, longitude);
        }
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
        if (position != null) {
            this.latitude = position.latitude;
            this.longitude = position.longitude;
        }
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
        updatePosition();
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
        updatePosition();
    }

    private void updatePosition() {
        if (latitude != 0 && longitude != 0) {
            position = new LatLng(latitude, longitude);
        }
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public List<String> getComments() {
        return comments;
    }

    public void setComments(List<String> comments) {
        this.comments = comments;
    }

    public int getUpvotes() {
        return upvotes;
    }

    public void setUpvotes(int upvotes) {
        this.upvotes = upvotes;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}