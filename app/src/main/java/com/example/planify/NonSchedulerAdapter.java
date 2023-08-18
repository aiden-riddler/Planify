package com.example.planify;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NonSchedulerAdapter extends RecyclerView.Adapter<NonSchedulerAdapter.TaskViewHolder>{

    private List<NonScheduleTime> nonScheduleTimes;
    private Context context;

    public NonSchedulerAdapter(List<NonScheduleTime> nonScheduleTimes, Context context) {
        this.nonScheduleTimes = nonScheduleTimes;
        this.context = context;
    }

    public List<NonScheduleTime> getNonScheduleTimes() {
        return nonScheduleTimes;
    }

    public void setNonScheduleTimes(List<NonScheduleTime> nonScheduleTimes) {
        this.nonScheduleTimes = nonScheduleTimes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.non_schedule_hr, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, @SuppressLint("RecyclerView") int position) {
        NonScheduleTime nonScheduleTime = nonScheduleTimes.get(position);
        holder.dayView.setText(getDayOfWeekFromDate(nonScheduleTime.getDay()));
        String time = formatTime(nonScheduleTime.getStartTime()) + "-" + formatTime(nonScheduleTime.getEndtime());
        holder.timeView.setText(time);
    }

    @Override
    public int getItemCount() {
        return nonScheduleTimes.size();
    }

    // Format time to HH:mm AM/PM
    private String formatTime(long timeInMillis) {
        // Format the time using SimpleDateFormat or any other method you prefer
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm a", Locale.getDefault());
        return sdf.format(timeInMillis);
    }

    public String getDayOfWeekFromDate(int dayOfWeekValue) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE", Locale.getDefault());

        // Get the day name based on the day of the week value
        Date date = new Date();
        date.setDate(dayOfWeekValue); // Set the date to the day of the week value
        String dayOfWeek = dateFormat.format(date);

        return dayOfWeek;
    }

    class TaskViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView dayView;
        TextView timeView;
        Button delete;

        TaskViewHolder(View itemView) {
            super(itemView);
            dayView = itemView.findViewById(R.id.day);
            timeView = itemView.findViewById(R.id.timeTextView);
            delete = itemView.findViewById(R.id.delete);
            delete.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            DatabaseHelper db = new DatabaseHelper(context);
            NonScheduleTime nonScheduleTime = nonScheduleTimes.get(getAdapterPosition());
            String time = formatTime(nonScheduleTime.getStartTime()) + "-" + formatTime(nonScheduleTime.getEndtime());
            if (view.getId() == delete.getId()){
                // delete
                new MaterialAlertDialogBuilder(context)
                        .setTitle("Delete Time")
                        .setMessage("The time range " + time + " will be deleted and tasks will be scheduled at this times.")
                        .setPositiveButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        }).setNegativeButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                db.deleteNonScheduleHr(nonScheduleTime.getId());
                                setNonScheduleTimes(db.getNonSchedulableHours());
                                dialogInterface.cancel();
                            }
                        }).show();
            }
        }
    }

}

