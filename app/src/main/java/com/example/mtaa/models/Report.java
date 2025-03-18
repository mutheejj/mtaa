package com.example.mtaa.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Report implements ClusterItem {
    @DocumentId
    private String id;
    private String userId;
    private String title;
    private String description;
    private String category;
    private GeoPoint location;
    private String imageUrl;
    private int upvotes;
    private String status;
    @ServerTimestamp
    private Date createdAt;
    private float zindex;
    private String comments;
    private double latitude;
    private String userName;
    private String content;

    // Empty constructor required for Firestore
    public Report() {
        // Initialize default values to prevent null pointer exceptions
        this.upvotes = 0;
        this.status = "pending";
    }

    public Report(@NonNull String userId, @NonNull String title, @NonNull String description,
                 @NonNull String category, @Nullable GeoPoint location, @Nullable String imageUrl) {
        this();
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.location = location;
        this.imageUrl = imageUrl;
    }

    public Report(@NonNull String userId, @NonNull String title, @NonNull String description,
                 @NonNull String category, @Nullable GeoPoint location, @Nullable String imageUrl,
                 @NonNull com.google.firebase.Timestamp timestamp) {
        this(userId, title, description, category, location, imageUrl);
        this.createdAt = timestamp.toDate();
    }

    public void setPosition(LatLng position) {
        if (position != null) {
            this.location = new GeoPoint(position.latitude, position.longitude);
        }
    }

    @NonNull
    public String getId() { return id != null ? id : ""; }
    public void setId(@NonNull String id) { this.id = id; }

    @NonNull
    public String getUserId() { return userId != null ? userId : ""; }
    public void setUserId(@NonNull String userId) { this.userId = userId; }

    @NonNull
    public String getTitle() { return title != null ? title : ""; }
    public void setTitle(@NonNull String title) { this.title = title; }

    @NonNull
    public String getDescription() { return description != null ? description : ""; }
    public void setDescription(@NonNull String description) { this.description = description; }

    @NonNull
    public String getCategory() { return category != null ? category : ""; }
    public void setCategory(@NonNull String category) { this.category = category; }

    @Nullable
    public GeoPoint getLocation() { return location; }
    public void setLocation(@Nullable GeoPoint location) { this.location = location; }

    @Nullable
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(@Nullable String imageUrl) { this.imageUrl = imageUrl; }

    public int getUpvotes() { return upvotes; }
    public void setUpvotes(int upvotes) { this.upvotes = upvotes; }

    @NonNull
    public String getStatus() { return status != null ? status : "pending"; }
    public void setStatus(@NonNull String status) { this.status = status; }

    @Nullable
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(@Nullable Date createdAt) { this.createdAt = createdAt; }

    public float getZindex() { return zindex; }
    public void setZindex(float zindex) { this.zindex = zindex; }

    public String getComments() { return comments != null ? comments : ""; }
    public void setComments(String comments) { this.comments = comments; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public String getUserName() { return userName != null ? userName : ""; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getContent() { return content != null ? content : ""; }
    public void setContent(String content) { this.content = content; }

    @Exclude
    public boolean isValid() {
        // Relaxed validation for admin access
        return title != null && !title.isEmpty() &&
               description != null && !description.isEmpty() &&
               category != null && !category.isEmpty();
    }

    public long getTimestamp() {
        return createdAt != null ? createdAt.getTime() : 0;
    }

    @Override
    public LatLng getPosition() {
        return location != null ? new LatLng(location.getLatitude(), location.getLongitude()) : null;
    }

    @Override
    public Float getZIndex() {
        return 0.0f;
    }

    @Override
    public String getSnippet() {
        return description;
    }
}