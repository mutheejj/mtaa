package com.example.mtaa.models;

import com.google.firebase.firestore.GeoPoint;
import java.util.Date;

public class Report {
    private String id;
    private String userId;
    private String title;
    private String description;
    private String category;
    private GeoPoint location;
    private String imageUrl;
    private int upvotes;
    private String status;
    private Date createdAt;

    public Report() {
        // Required empty constructor for Firestore
    }

    public Report(String userId, String title, String description, String category,
                 GeoPoint location, String imageUrl) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.location = location;
        this.imageUrl = imageUrl;
        this.upvotes = 0;
        this.status = "pending";
        this.createdAt = new Date();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public GeoPoint getLocation() { return location; }
    public void setLocation(GeoPoint location) { this.location = location; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public int getUpvotes() { return upvotes; }
    public void setUpvotes(int upvotes) { this.upvotes = upvotes; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
} 