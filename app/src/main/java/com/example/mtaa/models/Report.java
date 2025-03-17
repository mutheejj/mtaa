package com.example.mtaa.models;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.GeoPoint;
import com.google.maps.android.clustering.ClusterItem;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;

public class Report implements ClusterItem {
    private String id;
    private String userId;
    private String userName;
    private String content;
    private Timestamp timestamp;
    private int likes;
    private List<String> comments = new ArrayList<>();
    private double latitude;
    private double longitude;
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
        this.upvotes = 0;
        this.comments = new ArrayList<>();
        this.likes = 0;
        this.status = "pending";
        this.createdAt = new Date();
        this.latitude = 0.0;
        this.longitude = 0.0;
        this.timestamp = new Timestamp(new Date());
    }

    public Report(String userId, String title, String description, String category,
                 GeoPoint location, String imageUrl, Timestamp timestamp) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.location = location;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
        this.upvotes = 0;
        this.status = "pending";
        this.createdAt = new Date();
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
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
    
    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }
    
    public List<String> getComments() { return comments; }
    public void setComments(List<String> comments) { this.comments = comments; }
    
    @com.google.firebase.firestore.PropertyName("comments")
    public void setCommentsFromFirestore(Object commentsObj) {
        if (commentsObj instanceof List) {
            this.comments = (List<String>) commentsObj;
        } else if (commentsObj instanceof Integer) {
            this.comments = new ArrayList<>();
        } else {
            this.comments = new ArrayList<>();
        }
    }
    
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public GeoPoint getLocation() { return location; }
    public void setLocation(GeoPoint location) { this.location = location; }
    
    public void setPosition(LatLng position) {
        this.location = new GeoPoint(position.latitude, position.longitude);
    }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public int getUpvotes() { return upvotes; }
    public void setUpvotes(int upvotes) { this.upvotes = upvotes; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    @Override
    public LatLng getPosition() {
        if (location != null) {
            return new LatLng(location.getLatitude(), location.getLongitude());
        } else if (latitude != 0 && longitude != 0) {
            return new LatLng(latitude, longitude);
        }
        return null;
    }

    @Override
    public String getSnippet() {
        return category;
    }

    @Override
    public Float getZIndex() {
        return 1.0f;
    }
}