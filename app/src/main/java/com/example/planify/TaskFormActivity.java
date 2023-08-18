package com.example.planify;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TaskFormActivity extends AppCompatActivity {
    private ImageButton addSubTask;
    private ImageButton removeSubTask;
    private int subTaskCounter = 0;
    private LinearLayout subTasksLayout;
    private AutoCompleteTextView editCourseName;
    private EditText  editTaskName, subTask1Activity, subTask1Hours, subTask1Minutes, subTask1Secs;
    private DatePicker datePicker;
    private TimePicker timePickerStart, timePickerEnd;
    private Button submitButton;
    private DatabaseHelper db;

    // update variables
    private boolean isUpdate = false;
    private Task previousTask;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_form);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        //initialize db
        db = new DatabaseHelper(this);

        // Initialize views
        editCourseName = findViewById(R.id.editCourseName);
        editTaskName = findViewById(R.id.editTaskName);
        subTasksLayout = findViewById(R.id.subTasksLayout);
        datePicker = findViewById(R.id.datePicker);
        timePickerStart = findViewById(R.id.timePickerStart);
        timePickerEnd = findViewById(R.id.timePickerEnd);
        submitButton = findViewById(R.id.submitButton);
        addSubTask = findViewById(R.id.add);
        removeSubTask = findViewById(R.id.remove);
        subTask1Activity = findViewById(R.id.subTask1Activity);
        subTask1Hours = findViewById(R.id.subTask1Hours);
        subTask1Minutes = findViewById(R.id.subTask1Minutes);
        subTask1Secs = findViewById(R.id.subTask1Secs);

        // set up spinner
        String[] courses = db.getCourses();
        ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, courses);
        editCourseName.setAdapter(courseAdapter);

        // get intent
        String taskId = getIntent().getStringExtra("TaskID");
        if (taskId != null){
            isUpdate = true;
            previousTask = db.getTaskById(Integer.valueOf(taskId));
            editCourseName.setText(previousTask.getCourse());
            editTaskName.setText(previousTask.getTaskName());
            List<SubTask> tempPreviousSubTasks = previousTask.getSubTasks();
            if (tempPreviousSubTasks.size() > 0){
                SubTask temp = tempPreviousSubTasks.get(0);
                subTask1Activity.setText(temp.getSubTaskName());
                int[] tempTime = convertSecondsToHMS(temp.getTimeRequired());
                subTask1Hours.setText(String.valueOf(tempTime[0]));
                subTask1Minutes.setText(String.valueOf(tempTime[1]));
                subTask1Secs.setText(String.valueOf(tempTime[2]));

                for (int i=1; i<tempPreviousSubTasks.size(); i++) {
                    SubTask tempSubTask = tempPreviousSubTasks.get(i);
                    int[] tempSubTaskTime = convertSecondsToHMS(tempSubTask.getTimeRequired());
                    addSubTaskLayout(tempSubTask.getSubTaskName(), tempSubTaskTime[0], tempSubTaskTime[1], tempSubTaskTime[2]);
                }
            }
            // Set the desired date
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(previousTask.getTaskStartTime());

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            datePicker.updateDate(year, month, day);

            // starttime
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            timePickerStart.setIs24HourView(false); // Set 12-hour format
            timePickerStart.setCurrentHour(hour);
            timePickerStart.setCurrentMinute(minute);

            //end time
            calendar.setTimeInMillis(previousTask.getTaskEndTime());

            hour = calendar.get(Calendar.HOUR_OF_DAY);
            minute = calendar.get(Calendar.MINUTE);

            timePickerEnd.setIs24HourView(false); // Set 12-hour format
            timePickerEnd.setCurrentHour(hour);
            timePickerEnd.setCurrentMinute(minute);

        }

        // Handle onclick
        addSubTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSubTaskLayout();
            }
        });

        removeSubTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeSubTaskLayout();
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Collect data
                String courseName = toCamelCase(editCourseName.getText().toString());
                String taskName = toSentenceCase(editTaskName.getText().toString());
                List<SubTask> subTasks = collectSubTasks();
                int year = datePicker.getYear();
                int month = datePicker.getMonth();
                int day = datePicker.getDayOfMonth();
                int startHour = timePickerStart.getHour();
                int startMinute = timePickerStart.getMinute();
                int endHour = timePickerEnd.getHour();
                int endMinute = timePickerEnd.getMinute();

                Log.d("Planify", "Year: " + year + " Month: " + month + " Day: " + day + " Hour:" + startHour + " Minute: " + startMinute + " Hour:" + endHour + " Minute: " + endMinute);

                // get time
                Calendar c1 = Calendar.getInstance();
                c1.set(Calendar.YEAR, year);
                c1.set(Calendar.MONTH, month);
                c1.set(Calendar.DAY_OF_MONTH, day);

                // for start time
                c1.set(Calendar.HOUR_OF_DAY, startHour);
                c1.set(Calendar.MINUTE, startMinute);
                c1.set(Calendar.SECOND, 0);
                c1.set(Calendar.MILLISECOND, 0);

                long startTime = c1.getTimeInMillis();
                // NOTIFICATION TIME
                c1.add(Calendar.MINUTE, -10);
                long notTime = c1.getTimeInMillis();

                // for end time
                c1.set(Calendar.HOUR_OF_DAY, endHour);
                c1.set(Calendar.MINUTE, endMinute);
                c1.set(Calendar.SECOND, 0);
                c1.set(Calendar.MILLISECOND, 0);

                long endTime = c1.getTimeInMillis();

                int totalTimeRequired = 0;
                for (SubTask subTask:subTasks)
                    totalTimeRequired += subTask.getTimeRequired() * 1000;

                Log.d("Planify", "Subtasks size: " + subTasks.size() + " Subtask counter: " + (subTaskCounter + 1));
                // check for empty inputs
                if (courseName.isEmpty() || taskName.isEmpty())
                    Toast.makeText(TaskFormActivity.this, "Fill empty fields", Toast.LENGTH_SHORT).show();
                else if (subTasks.size() == 0 || (subTaskCounter + 1) > subTasks.size())
                    Toast.makeText(TaskFormActivity.this, "Fill empty subtask fields. Time required must be greater than zero for each subtask.", Toast.LENGTH_SHORT).show();
                else if (totalTimeRequired > (endTime - startTime))
                    Toast.makeText(TaskFormActivity.this, "Time provided between start-time and end-time is not enough for subtasks.", Toast.LENGTH_SHORT).show();
                else{
                    if (isUpdate){
                        // cancel previous notifications
                        List<Schedule> schedules = db.getAllSchedulesForTask(previousTask.getId());
                        for (Schedule schedule:schedules){
                            NotificationUtils.cancelNotification(TaskFormActivity.this, schedule.getNotificationId());
                            db.deleteSchedule(schedule.getId());
                        }

                        Task task = new Task(courseName, taskName, startTime, endTime, false, 0.0, false);
                        task.setId(previousTask.getId());
                        task.setSubTasks(subTasks);

                        int rows = (int) db.updateTask(task);
                        if (rows > 0){
                            // Do something with the collected data
                            String message = "Start task: " + taskName;
                            int notId = uniqueNotificationId();
                            NotificationUtils.scheduleNotification(TaskFormActivity.this, message, startTime, notId);

                            message = "Upcoming task: " + taskName;
                            NotificationUtils.scheduleNotification(TaskFormActivity.this, message, notTime, notId);

                            db.insertSchedule(new Schedule(task.getId(), notId));
                            Intent intent = new Intent(TaskFormActivity.this, MainActivity.class);
                            startActivity(intent);
                        } else if (rows == -1) {
                            Toast.makeText(TaskFormActivity.this, "Time selected is already occupied", Toast.LENGTH_SHORT).show();

                        } else if (rows == -2){
                            Toast.makeText(TaskFormActivity.this, "Cannot schedule task at time selected.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Do something with the collected data

                        Task task = new Task(courseName, taskName, startTime, endTime, false, 0.0, false);
                        task.setSubTasks(subTasks);
                        Log.d("Planify", "Size of subTasks: " + task.getSubTasks().size());
                        for(SubTask subTask:subTasks)
                            Log.d("Planify", " Added subtasks: " + subTask.toString());
                        int taskId = (int) db.insertTask(task);
                        if (taskId > 0){
                            String message = "Start task: " + taskName;
                            int notId = uniqueNotificationId();
                            NotificationUtils.scheduleNotification(TaskFormActivity.this, message, startTime, notId);

                            message = "Upcoming task: " + taskName;
                            NotificationUtils.scheduleNotification(TaskFormActivity.this, message, notTime, notId);

                            db.insertSchedule(new Schedule(taskId, notId));
                            Intent intent = new Intent(TaskFormActivity.this, MainActivity.class);
                            startActivity(intent);
                        } else if (taskId == -1) {
                            Toast.makeText(TaskFormActivity.this, "Time selected is already occupied", Toast.LENGTH_SHORT).show();
                        } else if (taskId == -2){
                            Toast.makeText(TaskFormActivity.this, "Cannot schedule task at time selected.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });

    }

    public int[] convertSecondsToHMS(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int remainingSeconds = seconds % 60;
//        String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
        return new int[]{hours, minutes, remainingSeconds};
    }

    private String toSentenceCase(String sentence){
        return sentence.substring(0, 1).toUpperCase() + sentence.substring(1).toLowerCase();
    }

    private String toCamelCase(String sentence){
        String[] words = sentence.split(" ");
        StringBuilder capitalized = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                capitalized.append(word.substring(0, 1).toUpperCase())
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }

        return capitalized.toString().trim();
    }

    private static int uniqueNotificationId() {
        return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
    }

    private List<SubTask> collectSubTasks() {
        List<SubTask> subTasks = new ArrayList<>();
        // Collect subtask data from subTasksLayout and add to subTasks list
        // Loop through subTasksLayout's child views and collect data from each view

        //get subtask 1
        String subTask1Name = subTask1Activity.getText().toString();
        int subTask1Hr = Integer.parseInt(subTask1Hours.getText().toString().isEmpty() ? "0" : subTask1Hours.getText().toString());
        int subTask1Min = Integer.parseInt(subTask1Minutes.getText().toString().isEmpty() ? "0" : subTask1Minutes.getText().toString());
        int subTask1Sec = Integer.parseInt(subTask1Secs.getText().toString().isEmpty() ? "0" : subTask1Secs.getText().toString());
        int subTask1timeReq = (subTask1Hr * 60 * 60) + (subTask1Min * 60) + (subTask1Sec);

        if (subTask1Name.isEmpty() || subTask1timeReq == 0)
            return subTasks;

        SubTask subTask1 = new SubTask();
        subTask1.setSubTaskName(subTask1Name);
        subTask1.setTimeRequired(subTask1timeReq);
        subTask1.setPosition(1);
        if (isUpdate) {
            if (previousTask.getSubTasks().size() > 0)
                subTask1.setId(previousTask.getSubTasks().get(0).getId());
        }
        subTasks.add(subTask1);

        int counterCopy = subTaskCounter;
        int counter = 0;
        while (counterCopy > 0) {
            String subTaskName = ((EditText) findViewById(100+counterCopy)).getText().toString();
            int subTaskHr = Integer.parseInt(((EditText) findViewById(200+counterCopy)).getText().toString().isEmpty() ? "0" : ((EditText) findViewById(200+counterCopy)).getText().toString());
            int subTaskMin = Integer.parseInt(((EditText) findViewById(300+counterCopy)).getText().toString().isEmpty() ? "0" : ((EditText) findViewById(300+counterCopy)).getText().toString());
            int subTaskSec = Integer.parseInt(((EditText) findViewById(400+counterCopy)).getText().toString().isEmpty() ? "0" : ((EditText) findViewById(400+counterCopy)).getText().toString());
            int timeReq = (subTaskHr * 60 * 60) + (subTaskMin * 60) + (subTaskSec);
            counterCopy--;

            if (subTaskName.isEmpty() || timeReq == 0)
                continue;
            SubTask subTask = new SubTask();
            subTask.setSubTaskName(subTaskName);
            subTask.setTimeRequired(timeReq);

            if (isUpdate){
                List<SubTask> previousSubTasks = previousTask.getSubTasks();
                if (counter < previousSubTasks.size()){
                    subTask.setId(previousSubTasks.get(counter).getId());
                }
            }
            subTask.setPosition(counter + 2);
            subTasks.add(subTask);
            counter++;
        }
        return subTasks;
    }

    private void addSubTaskLayout(){
        subTaskCounter++;
        // Create a horizontal LinearLayout
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setId(subTaskCounter);

        // Create the first EditText with layout weight 3
        EditText editTextActivity = new EditText(this);
        editTextActivity.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                4));
        editTextActivity.setId(100 + subTaskCounter);
        editTextActivity.setHint("Activity");

        // Set the maximum length (number of characters) for the EditText
        int maxLength = 10; // Change this to your desired maximum length
        InputFilter[] inputFilters = new InputFilter[] { new InputFilter.LengthFilter(maxLength) };


        // Create the second EditText with layout weight 1
        EditText editTextHours = new EditText(this);
        editTextHours.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1));
        editTextHours.setInputType(InputType.TYPE_CLASS_NUMBER);
        editTextHours.setId(200 + subTaskCounter);
        editTextHours.setHint("Hrs");
        editTextHours.setFilters(inputFilters);


        TextView colonTextView = new TextView(this);
        colonTextView.setText(":");
        colonTextView.setTextSize(24); // in SP
        colonTextView.setTypeface(null, Typeface.BOLD);

        // Create the third EditText with layout weight 1
        EditText editTextMinutes = new EditText(this);
        editTextMinutes.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1));
        editTextMinutes.setInputType(InputType.TYPE_CLASS_NUMBER);
        editTextMinutes.setId(300 + subTaskCounter);
        editTextMinutes.setHint("Mins");
        maxLength = 59;
        inputFilters = new InputFilter[] { new InputFilter.LengthFilter(maxLength) };
        editTextMinutes.setFilters(inputFilters);

        TextView colonTextView2 = new TextView(this);
        colonTextView2.setText(":");
        colonTextView2.setTextSize(24); // in SP
        colonTextView2.setTypeface(null, Typeface.BOLD);

        // Create the fourth EditText with layout weight 1
        EditText editTextSeconds = new EditText(this);
        editTextMinutes.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1));
        editTextSeconds.setInputType(InputType.TYPE_CLASS_NUMBER);
        editTextSeconds.setId(400 + subTaskCounter);
        editTextSeconds.setHint("Secs");
        editTextSeconds.setFilters(inputFilters);

        // Add the EditTexts to the LinearLayout
        linearLayout.addView(editTextActivity);
        linearLayout.addView(editTextHours);
        linearLayout.addView(colonTextView);
        linearLayout.addView(editTextMinutes);
        linearLayout.addView(colonTextView2);
        linearLayout.addView(editTextSeconds);

        subTasksLayout.addView(linearLayout);

    }

    private void addSubTaskLayout(String name, int hrs, int mins, int secs){
        subTaskCounter++;
        // Create a horizontal LinearLayout
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setId(subTaskCounter);

        // Create the first EditText with layout weight 3
        EditText editTextActivity = new EditText(this);
        editTextActivity.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                4));
        editTextActivity.setId(100 + subTaskCounter);
        editTextActivity.setHint("Activity");
        editTextActivity.setText(name);

        // Set the maximum length (number of characters) for the EditText
        int maxLength = 10; // Change this to your desired maximum length
        InputFilter[] inputFilters = new InputFilter[] { new InputFilter.LengthFilter(maxLength) };


        // Create the second EditText with layout weight 1
        EditText editTextHours = new EditText(this);
        editTextHours.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1));
        editTextHours.setInputType(InputType.TYPE_CLASS_NUMBER);
        editTextHours.setId(200 + subTaskCounter);
        editTextHours.setHint("Hrs");
        editTextHours.setFilters(inputFilters);
        editTextHours.setText(String.valueOf(hrs));


        TextView colonTextView = new TextView(this);
        colonTextView.setText(":");
        colonTextView.setTextSize(24); // in SP
        colonTextView.setTypeface(null, Typeface.BOLD);

        // Create the third EditText with layout weight 1
        EditText editTextMinutes = new EditText(this);
        editTextMinutes.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1));
        editTextMinutes.setInputType(InputType.TYPE_CLASS_NUMBER);
        editTextMinutes.setId(300 + subTaskCounter);
        editTextMinutes.setHint("Mins");
        maxLength = 59;
        inputFilters = new InputFilter[] { new InputFilter.LengthFilter(maxLength) };
        editTextMinutes.setFilters(inputFilters);
        editTextMinutes.setText(String.valueOf(mins));


        TextView colonTextView2 = new TextView(this);
        colonTextView2.setText(":");
        colonTextView2.setTextSize(24); // in SP
        colonTextView2.setTypeface(null, Typeface.BOLD);

        // Create the fourth EditText with layout weight 1
        EditText editTextSeconds = new EditText(this);
        editTextSeconds.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1));
        editTextSeconds.setInputType(InputType.TYPE_CLASS_NUMBER);
        editTextSeconds.setId(400 + subTaskCounter);
        editTextSeconds.setHint("Secs");
        editTextSeconds.setFilters(inputFilters);
        editTextSeconds.setText(String.valueOf(secs));


        // Add the EditTexts to the LinearLayout
        linearLayout.addView(editTextActivity);
        linearLayout.addView(editTextHours);
        linearLayout.addView(colonTextView);
        linearLayout.addView(editTextMinutes);
        linearLayout.addView(colonTextView2);
        linearLayout.addView(editTextSeconds);

        subTasksLayout.addView(linearLayout);

    }

    private void removeSubTaskLayout(){
        if (subTaskCounter == 0)
            return;
        subTasksLayout.removeView(findViewById(subTaskCounter));
        subTaskCounter--;
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
        // get changes
        String courseName = editCourseName.getText().toString();
        String taskName = editTaskName.getText().toString();
        List<SubTask> subTasks = collectSubTasks();
        int year = datePicker.getYear();
        int month = datePicker.getMonth();
        int day = datePicker.getDayOfMonth();
        int startHour = timePickerStart.getHour();
        int startMinute = timePickerStart.getMinute();
        int endHour = timePickerEnd.getHour();
        int endMinute = timePickerEnd.getMinute();

        // get time
        Calendar c1 = Calendar.getInstance();
        c1.set(Calendar.YEAR, year);
        c1.set(Calendar.MONTH, month);
        c1.set(Calendar.DAY_OF_MONTH, day);

        // for start time
        c1.set(Calendar.HOUR_OF_DAY, startHour);
        c1.set(Calendar.MINUTE, startMinute);
        c1.set(Calendar.SECOND, 0);
        c1.set(Calendar.MILLISECOND, 0);
        long startTime = c1.getTimeInMillis();

        // for end time
        c1.set(Calendar.HOUR_OF_DAY, endHour);
        c1.set(Calendar.MINUTE, endMinute);
        c1.set(Calendar.SECOND, 0);
        c1.set(Calendar.MILLISECOND, 0);
        long endTime = c1.getTimeInMillis();

        boolean changes = false;

        // set previous time
        c1.setTimeInMillis(previousTask.getTaskStartTime());
        c1.set(Calendar.MILLISECOND, 0);
        long previousStartTime = c1.getTimeInMillis();
        c1.setTimeInMillis(previousTask.getTaskEndTime());
        c1.set(Calendar.MILLISECOND, 0);
        long previousEndTime = c1.getTimeInMillis();

//        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMM d yyyy HH:mm a", Locale.getDefault());
//        Log.d("Planify", "startTime: " + startTime + " -> " + sdf.format(startTime) + " Previous: " + previousStartTime + " -> " + sdf.format(previousStartTime));
//        Log.d("Planify", "endTime " + endTime + " -> " +sdf.format(endTime) + " Previous: " + previousEndTime + " -> " + sdf.format(previousEndTime));


        if (!courseName.equals(previousTask.getCourse())
                || !taskName.equals(previousTask.getTaskName())
                || startTime != previousStartTime
                || endTime != previousEndTime
                || previousTask.getSubTasks().size() != subTasks.size()
        ){
            Log.d("Planify", "Changes Exist here 1");
            changes = true;
        } else {
            for (int i=0; i<subTasks.size(); i++){
                SubTask a = subTasks.get(i);
                SubTask b = previousTask.getSubTasks().get(i);
                if (!a.getSubTaskName().equals(b.getSubTaskName()) || a.getTimeRequired() != b.getTimeRequired()){
                    changes = true;
                    Log.d("Planify", "Changes Exist here 2");
                    break;
                }
            }
        }

        if (changes){
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Discard Changes")
                    .setMessage("Changes made will be lost")
                    .setPositiveButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    }).setNegativeButton("DISCARD", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            TaskFormActivity.super.onBackPressed();
                        }
                    }).show();
        } else
            super.onBackPressed();

    }
}
