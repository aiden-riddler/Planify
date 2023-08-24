package com.example.planify;

import java.io.Serializable;

public class Course implements Serializable {
    private String courseName;
    private long startDate;
    private long endDate;
    private int subTasksCount = 0;
    private int completeSubTasksCount = 0;

    public Course() {

    }

    public Course(String courseName, long startDate, long endDate) {
        this.courseName = courseName;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public int getSubTasksCount() {
        return subTasksCount;
    }

    public void setSubTasksCount(int subTasksCount) {
        this.subTasksCount = subTasksCount;
    }

    public int getCompleteSubTasksCount() {
        return completeSubTasksCount;
    }

    public void setCompleteSubTasksCount(int completeSubTasksCount) {
        this.completeSubTasksCount = completeSubTasksCount;
    }
}
