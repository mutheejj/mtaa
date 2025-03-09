package com.example.mtaa.models;

import java.util.Date;

public class Achievement {
    private String title;
    private String description;
    private String iconUrl;
    private Date unlockedAt;
    private String userId;

    // Default constructor for Firebase
    public Achievement() {}

    public Achievement(String title, String description, String iconUrl, Date unlockedAt, String userId) {
        this.title = title;
        this.description = description;
        this.iconUrl = iconUrl;
        this.unlockedAt = unlockedAt;
        this.userId = userId;
    }

    public String getTitle() {
        return title;
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

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public Date getUnlockedAt() {
        return unlockedAt;
    }

    public void setUnlockedAt(Date unlockedAt) {
        this.unlockedAt = unlockedAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}