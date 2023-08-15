package com.example.planify;

public class Schedule {
    private int id;
    private int taskId;  // Foreign key to tasks
    private int notificationId;

    public Schedule() {
        // Default constructor
    }

    public Schedule(int taskId, int notificationId) {
        this.taskId = taskId;
        this.notificationId = notificationId;
    }

    // Getters and setters for all fields...

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }
}

