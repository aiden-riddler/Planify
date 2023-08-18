package com.example.planify;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Dashboard extends Fragment{

    private TextView dateTextView;
    private DataViewModel dataViewModel;
    private RecyclerView timeScaleRecyclerView;
    private TimeScaleAdapter timeScaleAdapter;
    private TaskAdapter taskAdapter;
    private DatabaseHelper db;
    private final Date[] currentDate = {new Date()};
    public Dashboard() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // initialize db
        db = new DatabaseHelper(getContext());
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        //initialize current date
        currentDate[0] = new Date();

        // initialize views
        ImageButton next = view.findViewById(R.id.next);
        ImageButton previous = view.findViewById(R.id.previous);

        // Find the TextView to display the formatted date
        dateTextView = view.findViewById(R.id.dateText);

        // Define the desired date format
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM d yyyy", Locale.getDefault());

        // Format the date and set it to the TextView
        String formattedDate = dateFormat.format(currentDate[0]);
        dateTextView.setText(formattedDate);

        dateTextView.setOnClickListener(v -> {
            showDatePicker();
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Date newDate = addToDate(currentDate[0], 1);
                currentDate[0] = newDate;
                setDateToTextView(newDate);
                taskAdapter.setTasks(db.getTasksOnDate(newDate), currentDate[0]);
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Date newDate = addToDate(currentDate[0], -1);
                currentDate[0] = newDate;
                setDateToTextView(newDate);
                taskAdapter.setTasks(db.getTasksOnDate(newDate), currentDate[0]);
            }
        });

        // set recyclers
//        timeScaleRecyclerView = view.findViewById(R.id.timeScaleRecyclerView);
//        timeScaleAdapter = new TimeScaleAdapter();
//        timeScaleRecyclerView.setAdapter(timeScaleAdapter);
//        timeScaleRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
//
//        List<TimeFrame> timeList = generateTimeList();
//        timeScaleAdapter.setTimeList(timeList);

        RecyclerView tasksRecycler = view.findViewById(R.id.tasksRecycler);
        FloatingActionButton addTaskFab = view.findViewById(R.id.addTaskFab);
        addTaskFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), TaskFormActivity.class);
                startActivity(intent);
            }
        });

        tasksRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        dataViewModel = new ViewModelProvider(requireActivity()).get(DataViewModel.class);
        taskAdapter = new TaskAdapter(getContext(), currentDate[0]);

        // Observe LiveData (if using LiveData)
         dataViewModel.getTaskListLiveData().observe(getViewLifecycleOwner(), tasks -> {
             taskAdapter.setTasks(tasks, currentDate[0]);
         });
        tasksRecycler.setAdapter(taskAdapter);
        return view;
    }

    private List<TimeFrame> generateTimeList() {
        List<TimeFrame> timeList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

        for (int i = 0; i < 24; i++) {
            calendar.set(Calendar.HOUR_OF_DAY, i);
            calendar.set(Calendar.MINUTE, 0);
            String formattedTime = timeFormat.format(calendar.getTime());
            timeList.add(new TimeFrame(formattedTime, 30, i));
        }

        return timeList;
    }

    public int[] convertSecondsToHMS(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int remainingSeconds = seconds % 60;
//        String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
        return new int[]{hours, minutes, remainingSeconds};
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, monthOfYear, dayOfMonth) -> {
                    calendar.set(year, monthOfYear, dayOfMonth);
                    Date selectedDate = calendar.getTime();
                    currentDate[0] = selectedDate;
                    setDateToTextView(selectedDate);
//                    dataViewModel.setTaskList(db.getTasksOnDate(selectedDate));
                    taskAdapter.setTasks(db.getTasksOnDate(selectedDate), currentDate[0]);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void setDateToTextView(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM d yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(date);
        dateTextView.setText(formattedDate);
    }

    private Date addToDate(Date date, int days){
        // Convert Date to Calendar
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        // Add one day to the Calendar
        calendar.add(Calendar.DAY_OF_MONTH, days);

        // Convert back to Date
        Date newDate = calendar.getTime();
        return newDate;
    }

}
