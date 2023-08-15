package com.example.planify;

public class FreeTime {
    private long startTime;
    private long endTime;

    public FreeTime(long startTime, long endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }
}
