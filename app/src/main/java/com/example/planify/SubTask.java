package com.example.planify;

import androidx.annotation.NonNull;

public class SubTask {
    private int id;
    private String subTaskName;
    private boolean subTaskCompleted = false;
    private int parentTaskId;  // Foreign key to tasks
    private int timeRequired;

    public SubTask() {
        // Default constructor
    }

    public SubTask(String subTaskName, boolean subTaskCompleted, int parentTaskId, int timeRequired) {
        this.subTaskName = subTaskName;
        this.subTaskCompleted = subTaskCompleted;
        this.parentTaskId = parentTaskId;
        this.timeRequired = timeRequired;
    }

    // Getters and setters for all fields...

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSubTaskName() {
        return subTaskName;
    }

    public void setSubTaskName(String subTaskName) {
        this.subTaskName = subTaskName;
    }

    public boolean isSubTaskCompleted() {
        return subTaskCompleted;
    }

    public void setSubTaskCompleted(boolean subTaskCompleted) {
        this.subTaskCompleted = subTaskCompleted;
    }

    public int getParentTaskId() {
        return parentTaskId;
    }

    public void setParentTaskId(int parentTaskId) {
        this.parentTaskId = parentTaskId;
    }

    public int getTimeRequired() {
        return timeRequired;
    }

    public void setTimeRequired(int timeRequired) {
        this.timeRequired = timeRequired;
    }

    @NonNull
    @Override
    public String toString() {
        return subTaskName + ", " + subTaskCompleted + ", " + parentTaskId + ", "+ timeRequired;
    }
}

