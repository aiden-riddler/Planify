package com.example.planify;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Task {
    private int id;
    private String course;
    private String taskName;
    private long taskStartTime;
    private long taskEndTime;
    private boolean isCompleted;
    private double completionPercentage;
    private boolean isDeleted;

    private List<SubTask> subTasks = new ArrayList<>();

    public Task() {
        // Default constructor
    }

    public Task(String course, String taskName,
                long taskStartTime, long taskEndTime, boolean isCompleted,
                double completionPercentage, boolean isDeleted) {
        this.course = course;
        this.taskName = taskName;
        this.taskStartTime = taskStartTime;
        this.taskEndTime = taskEndTime;
        this.isCompleted = isCompleted;
        this.completionPercentage = completionPercentage;
        this.isDeleted = isDeleted;
        this.subTasks = new ArrayList<>();
    }

    // Getters and setters for all fields...

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public long getTaskStartTime() {
        return taskStartTime;
    }

    public void setTaskStartTime(long taskStartTime) {
        this.taskStartTime = taskStartTime;
    }

    public long getTaskEndTime() {
        return taskEndTime;
    }

    public void setTaskEndTime(long taskEndTime) {
        this.taskEndTime = taskEndTime;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public double getCompletionPercentage() {
        return completionPercentage;
    }

    public void setCompletionPercentage(double completionPercentage) {
        this.completionPercentage = completionPercentage;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public List<SubTask> getSubTasks() {
        return subTasks;
    }

    public void setSubTasks(List<SubTask> subTasks) {
        this.subTasks = subTasks;
    }

    @NonNull
    @Override
    public String toString() {
        String subTaskString = "";
        for (SubTask subTask:subTasks)
            subTaskString += "( "+ subTask.toString() + " )\n";
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMM d yyyy HH:mm a", Locale.getDefault());
        return course + ", " + taskName + ", " + taskStartTime + ": " + sdf.format(taskStartTime) + ", " + taskEndTime + ": " + sdf.format(taskEndTime) + ", " + completionPercentage + ", " + isDeleted + "\n" + subTaskString;
    }
}

