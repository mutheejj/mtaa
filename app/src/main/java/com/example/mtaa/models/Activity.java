package com.example.mtaa.models;

import java.util.Date;

public class Activity {
    private String description;
    private Date timestamp;
    private String userId;
    private String type;
    private String referenceId;

    // Default constructor for Firebase
    public Activity() {}

    public Activity(String description, Date timestamp, String userId, String type, String referenceId) {
        this.description = description;
        this.timestamp = timestamp;
        this.userId = userId;
        this.type = type;
        this.referenceId = referenceId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }
}