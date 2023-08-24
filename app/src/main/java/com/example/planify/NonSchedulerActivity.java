package com.example.planify;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NonSchedulerActivity extends AppCompatActivity {
    private TimePicker timePickerStart, timePickerEnd;
    private Button submitButton;
    private CheckBox mon, tue, wed, thur, fri, sat, sun, nextDayCheck;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_non_scheduler);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // initialize views
        timePickerStart = findViewById(R.id.timePickerStart);
        timePickerEnd = findViewById(R.id.timePickerEnd);
        submitButton = findViewById(R.id.submitButton);
        mon = findViewById(R.id.mon);
        tue = findViewById(R.id.tue);
        wed = findViewById(R.id.wed);
        thur = findViewById(R.id.thur);
        fri = findViewById(R.id.fri);
        sat = findViewById(R.id.sat);
        sun = findViewById(R.id.sun);
        nextDayCheck = findViewById(R.id.nextDayCheck);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // collect data
                int startHour = timePickerStart.getHour();
                int startMinute = timePickerStart.getMinute();
                int endHour = timePickerEnd.getHour();
                int endMinute = timePickerEnd.getMinute();

                // for start time
                Calendar c1 = Calendar.getInstance();
                c1.set(Calendar.HOUR_OF_DAY, startHour);
                c1.set(Calendar.MINUTE, startMinute);
                c1.set(Calendar.SECOND, 0);
                c1.set(Calendar.MILLISECOND, 0);


                // for end time
                Calendar c2 = Calendar.getInstance();
                c2.set(Calendar.HOUR_OF_DAY, endHour);
                c2.set(Calendar.MINUTE, endMinute);
                c2.set(Calendar.SECOND, 0);
                c2.set(Calendar.MILLISECOND, 0);

                Log.d("Planify", "Start time: " + formatTime(c1.getTimeInMillis()) + " End time: " + formatTime(c2.getTimeInMillis()));


                if (!mon.isChecked() && !tue.isChecked() && !wed.isChecked() && !thur.isChecked()
                        && !fri.isChecked() && !sat.isChecked() && !sun.isChecked()){
                    Toast.makeText(NonSchedulerActivity.this, "Select at least one day.", Toast.LENGTH_SHORT).show();
                } else if (c1.getTimeInMillis() > c2.getTimeInMillis() && !nextDayCheck.isChecked()){
                    Toast.makeText(NonSchedulerActivity.this,
                            "End time cannot be before start time unless it's the next day",
                            Toast.LENGTH_SHORT).show();
                } else {
                    DatabaseHelper db = new DatabaseHelper(NonSchedulerActivity.this);
                    int res = 0;
                    String time = "";
                    if (sun.isChecked()){
                        c1.set(Calendar.DAY_OF_WEEK, 1);
                        c2.set(Calendar.DAY_OF_WEEK, 1);
                        long startTime = c1.getTimeInMillis();
                        if (nextDayCheck.isChecked())
                            c2.set(Calendar.DAY_OF_WEEK, 2);
                        long endTime = c2.getTimeInMillis();
                        time = formatTime(startTime) + " to " + formatTime(endTime);
                        res = db.insertNonSchedulableHour(startTime, endTime, 1);
                    }

                    if (res == -1) {
                        showToast(time);
                        return;
                    }

                    if (mon.isChecked()) {
                        c1.set(Calendar.DAY_OF_WEEK, 2);
                        c2.set(Calendar.DAY_OF_WEEK, 2);
                        long startTime = c1.getTimeInMillis();
                        if (nextDayCheck.isChecked())
                            c2.set(Calendar.DAY_OF_WEEK, 3);
                        long endTime = c2.getTimeInMillis();
                        time = formatTime(startTime) + " to " + formatTime(endTime);
                        res = db.insertNonSchedulableHour(startTime, endTime, 2);

                    }

                    if (res == -1) {
                        showToast(time);
                        return;
                    }

                    if (tue.isChecked()) {
                        c1.set(Calendar.DAY_OF_WEEK, 3);
                        c2.set(Calendar.DAY_OF_WEEK, 3);
                        long startTime = c1.getTimeInMillis();
                        if (nextDayCheck.isChecked())
                            c2.set(Calendar.DAY_OF_WEEK, 4);
                        long endTime = c2.getTimeInMillis();
                        time = formatTime(startTime) + " to " + formatTime(endTime);
                        res = db.insertNonSchedulableHour(startTime, endTime, 3);
                    }

                    if (res == -1) {
                        showToast(time);
                        return;
                    }

                    if (wed.isChecked()) {
                        c1.set(Calendar.DAY_OF_WEEK, 4);
                        c2.set(Calendar.DAY_OF_WEEK, 4);
                        long startTime = c1.getTimeInMillis();
                        if (nextDayCheck.isChecked())
                            c2.set(Calendar.DAY_OF_WEEK, 5);
                        long endTime = c2.getTimeInMillis();
                        time = formatTime(startTime) + " to " + formatTime(endTime);
                        res = db.insertNonSchedulableHour(startTime, endTime, 4);
                    }

                    if (res == -1) {
                        showToast(time);
                        return;
                    }

                    if (thur.isChecked()) {
                        c1.set(Calendar.DAY_OF_WEEK, 5);
                        c2.set(Calendar.DAY_OF_WEEK, 5);
                        long startTime = c1.getTimeInMillis();
                        if (nextDayCheck.isChecked())
                            c2.set(Calendar.DAY_OF_WEEK, 6);
                        long endTime = c2.getTimeInMillis();
                        time = formatTime(startTime) + " to " + formatTime(endTime);
                        res = db.insertNonSchedulableHour(startTime, endTime, 5);
                    }

                    if (res == -1) {
                        showToast(time);
                        return;
                    }

                    if (fri.isChecked()) {
                        c1.set(Calendar.DAY_OF_WEEK, 6);
                        c2.set(Calendar.DAY_OF_WEEK, 6);
                        long startTime = c1.getTimeInMillis();
                        if (nextDayCheck.isChecked())
                            c2.set(Calendar.DAY_OF_WEEK, 7);
                        long endTime = c2.getTimeInMillis();
                        time = formatTime(startTime) + " to " + formatTime(endTime);
                        res = db.insertNonSchedulableHour(startTime, endTime, 6);
                    }

                    if (res == -1) {
                        showToast(time);
                        return;
                    }

                    if (sat.isChecked()) {
                        c1.set(Calendar.DAY_OF_WEEK, 7);
                        c2.set(Calendar.DAY_OF_WEEK, 7);
                        long startTime = c1.getTimeInMillis();
                        if (nextDayCheck.isChecked())
                            c2.set(Calendar.DAY_OF_WEEK, 1);
                        long endTime = c2.getTimeInMillis();
                        time = formatTime(startTime) + " to " + formatTime(endTime);
                        res = db.insertNonSchedulableHour(startTime, endTime, 7);
                    }

                    if (res == -1) {
                        showToast(time);
                        return;
                    }

                    Intent intent = new Intent(NonSchedulerActivity.this, MainActivity.class);
                    startActivity(intent);
                }

            }
        });
    }

    private boolean checkConsecutiveDayCheck() {
        return (sun.isChecked() && mon.isChecked())
                || (mon.isChecked() && tue.isChecked())
                || (tue.isChecked() && wed.isChecked())
                || (wed.isChecked() && thur.isChecked())
                || (thur.isChecked() && fri.isChecked())
                || (fri.isChecked() && sat.isChecked())
                || (sat.isChecked() && sun.isChecked());
    }

    private void showToast(String time) {
        Toast.makeText(NonSchedulerActivity.this,
                time + ". Part of this timeframe or entire timeframe already exists in Non Schedule Time.",
                Toast.LENGTH_LONG).show();
    }

    private String formatTime(long timeInMillis) {
        // Format the time using SimpleDateFormat or any other method you prefer
        SimpleDateFormat sdf = new SimpleDateFormat("E, hh:mm a", Locale.getDefault());
        return sdf.format(timeInMillis);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // or navigate to the parent activity explicitly
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
