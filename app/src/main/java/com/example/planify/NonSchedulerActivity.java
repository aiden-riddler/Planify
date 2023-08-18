package com.example.planify;

import android.content.DialogInterface;
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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
                int startAmPmValue = c1.get(Calendar.AM_PM);

                long startTime = c1.getTimeInMillis();
                Log.d("Planify", "StartTime: " + formatTime(startTime));

                // for end time
                Calendar c2 = Calendar.getInstance();
                c2.set(Calendar.HOUR_OF_DAY, endHour);
                c2.set(Calendar.MINUTE, endMinute);
                c2.set(Calendar.SECOND, 0);
                c2.set(Calendar.MILLISECOND, 0);
                int endAmPmValue = c2.get(Calendar.AM_PM);


                if (nextDayCheck.isChecked())
                    c2.add(Calendar.DAY_OF_MONTH, 1);

                long endTime = c2.getTimeInMillis();
                Log.d("Planify", "EndTime: " + formatTime(endTime));

//                timePickerStart.setIs24HourView(true);
//                timePickerEnd.setIs24HourView(true);

                if (!mon.isChecked() && !tue.isChecked() && !wed.isChecked() && !thur.isChecked()
                        && !fri.isChecked() && !sat.isChecked() && !sun.isChecked()){
                    Toast.makeText(NonSchedulerActivity.this, "Select at least one day.", Toast.LENGTH_SHORT).show();
                } else {
                    DatabaseHelper db = new DatabaseHelper(NonSchedulerActivity.this);
                    if (sun.isChecked())
                        db.insertNonSchedulableHour(startTime, endTime, 0);
                    if (mon.isChecked())
                        db.insertNonSchedulableHour(startTime, endTime, 1);
                    if (tue.isChecked())
                        db.insertNonSchedulableHour(startTime, endTime, 2);
                    if (wed.isChecked())
                        db.insertNonSchedulableHour(startTime, endTime, 3);
                    if (thur.isChecked())
                        db.insertNonSchedulableHour(startTime, endTime, 4);
                    if (fri.isChecked())
                        db.insertNonSchedulableHour(startTime, endTime, 5);
                    if (sat.isChecked())
                        db.insertNonSchedulableHour(startTime, endTime, 6);

                    Intent intent = new Intent(NonSchedulerActivity.this, MainActivity.class);
                    startActivity(intent);
                }

            }
        });
    }

    private String formatTime(long timeInMillis) {
        // Format the time using SimpleDateFormat or any other method you prefer
        SimpleDateFormat sdf = new SimpleDateFormat("E, HH:mm a", Locale.getDefault());
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
