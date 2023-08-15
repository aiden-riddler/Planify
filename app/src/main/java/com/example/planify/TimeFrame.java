package com.example.planify;

public class TimeFrame {
    String stringTime;
    int height;
    int hourOfDay;

    public TimeFrame(String stringTime, int height, int hourOfDay) {
        this.stringTime = stringTime;
        this.height = height;
        this.hourOfDay = hourOfDay;
    }

    public String getStringTime() {
        return stringTime;
    }

    public void setStringTime(String stringTime) {
        this.stringTime = stringTime;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getHourOfDay() {
        return hourOfDay;
    }

    public void setHourOfDay(int hourOfDay) {
        this.hourOfDay = hourOfDay;
    }
}
