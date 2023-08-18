package com.example.planify;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class FreeTime {
    private long startTime;
    private long endTime;
    private boolean isImmediate;
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

    public boolean isImmediate() {
        return isImmediate;
    }

    public void setImmediate(boolean immediate) {
        isImmediate = immediate;
    }

    public long getDuration(){
        if (endTime != 0)
            return endTime - startTime;
        else
            return 0;
    }
    private SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMM d yyyy HH:mm a", Locale.getDefault());

    @NonNull
    @Override
    public String toString() {
        return sdf.format(startTime) + " ---> " +sdf.format(endTime);
    }
}
