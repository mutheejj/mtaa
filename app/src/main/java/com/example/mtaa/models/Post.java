package com.example.mtaa.models;

import com.google.firebase.Timestamp;

public class Post {
    private String id;
    private String userId;
    private String userName;
    private String content;
    private Timestamp timestamp;
    private int likes;
    private int comments;

    // Required empty constructor for Firestore
    public Post() {}

    // Getters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getContent() { return content; }
    public Timestamp getTimestamp() { return timestamp; }
    public int getLikes() { return likes; }
    public int getComments() { return comments; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setContent(String content) { this.content = content; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
    public void setLikes(int likes) { this.likes = likes; }
    public void setComments(int comments) { this.comments = comments; }
} 