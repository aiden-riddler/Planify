package com.example.planify;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> implements SubTasksAdapterListener{

    private List<Task> taskList;
    private Context context;
    private Date date;

    public TaskAdapter(Context context, Date date) {
        this.taskList = new ArrayList<>();
        this.context = context;
        this.date = date;
    }

    public void setTaskList(List<Task> taskList) {
        this.taskList = taskList;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Task task = taskList.get(position);

        holder.courseTextView.setText(task.getCourse());
        holder.taskNameTextView.setText(task.getTaskName());
        // Set other views...

        // Format and set start and end times
        String startTime = formatTime(task.getTaskStartTime())[1] + " " + formatTime(task.getTaskStartTime())[0];
        String endTime = formatTime(task.getTaskEndTime())[0];
        holder.timeTextView.setText(startTime + " - " + endTime);

        // Set completion status and percentage
        if (task.isCompleted()) {
            holder.delete.setEnabled(false);
            holder.update.setEnabled(false);
            holder.forward.setEnabled(false);
            holder.delete.setColorFilter(Color.GRAY);
            holder.update.setColorFilter(Color.GRAY);
            holder.forward.setColorFilter(Color.GRAY);
            holder.progressBar.setProgress(100);
        } else {
            holder.delete.setEnabled(true);
            holder.update.setEnabled(true);
            holder.forward.setEnabled(true);
            holder.delete.setColorFilter(Color.RED);
            holder.update.setColorFilter(Color.BLUE);
            holder.forward.setColorFilter(Color.MAGENTA);
            holder.progressBar.setProgress((int) task.getCompletionPercentage());
        }

        // Set up subtask RecyclerView
        SubTaskAdapter subTaskAdapter = new SubTaskAdapter(task.getSubTasks(), context);
        subTaskAdapter.setListener(this);
        holder.subTaskRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        holder.subTaskRecyclerView.setAdapter(subTaskAdapter);

    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    // Format time to HH:mm AM/PM
    private String[] formatTime(long timeInMillis) {
        // Format the time using SimpleDateFormat or any other method you prefer
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        SimpleDateFormat sdf2 = new SimpleDateFormat("E,", Locale.getDefault());
        return new String[]{sdf.format(new Date(timeInMillis)), sdf2.format(new Date(timeInMillis))};
    }

    @Override
    public void onDataChanged(int taskId) {
        for(int i=0; i<taskList.size(); i++){
            Task task = taskList.get(i);
            if (taskId == task.getId()){
                DatabaseHelper db = new DatabaseHelper(context);
                taskList.remove(i);
                taskList.add(i, db.getTaskById(task.getId()));
                notifyDataSetChanged();
                break;
            }
        }
    }

    @Override
    public void onEntireUpdateRequired() {
        DatabaseHelper db = new DatabaseHelper(context);
        List<Task> tasksOnDate = db.getTasksOnDate(this.date);
        this.taskList = tasksOnDate;
        notifyDataSetChanged();
    }

    public void setTasks(List<Task> tasks, Date date) {
        Log.d("Planify", "setting tasks " + tasks.size());
        this.taskList = tasks;
        this.date = date;
        notifyDataSetChanged();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView courseTextView;
        TextView taskNameTextView;
        TextView timeTextView;
        RecyclerView subTaskRecyclerView;
        ImageButton delete;
        ImageButton update;
        ImageButton forward;
        ProgressBar progressBar;

        TaskViewHolder(View itemView) {
            super(itemView);
            courseTextView = itemView.findViewById(R.id.courseTextView);
            taskNameTextView = itemView.findViewById(R.id.taskNameTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            subTaskRecyclerView = itemView.findViewById(R.id.subTaskRecyclerView);
            delete = itemView.findViewById(R.id.delete);
            delete.setOnClickListener(this);
            update = itemView.findViewById(R.id.update);
            update.setOnClickListener(this);
            forward = itemView.findViewById(R.id.forward);
            forward.setOnClickListener(this);
            progressBar = itemView.findViewById(R.id.progressBar);
        }

        @Override
        public void onClick(View view) {
            Task task = taskList.get(getAdapterPosition());
            DatabaseHelper db = new DatabaseHelper(context);
            if (view.getId() == delete.getId()){
                new MaterialAlertDialogBuilder(context)
                        .setTitle("Delete task")
                        .setMessage("The " + task.getTaskName() + " task will be deleted.")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            // Handle positive button click
                            task.setDeleted(true);
                            db.updateTask(task);
                            List<Schedule> schedules = db.getAllSchedulesForTask(task.getId());
                            for (Schedule schedule:schedules){
                                NotificationUtils.cancelNotification(context, schedule.getNotificationId());
                                NotificationUtils.cancelNotification(context, schedule.getNotificationId());
                                db.deleteSchedule(schedule.getId());
                            }
                            // notify change
                            taskList.remove(getAdapterPosition());
                            notifyDataSetChanged();
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            // Handle negative button click
                        }).show();
            } else if (view.getId() == update.getId()){
                Intent intent = new Intent(context, TaskFormActivity.class);
                intent.putExtra("TaskID", String.valueOf(task.getId()));
                context.startActivity(intent);
            } else if (view.getId() == forward.getId()){
                View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_layout_2, null);
                CheckBox hrsCheck = dialogView.findViewById(R.id.hrsCheck);
                CheckBox dayCheck = dialogView.findViewById(R.id.daysCheck);
                CheckBox weeksCheck = dialogView.findViewById(R.id.weeksCheck);
                EditText hrsEditText = dialogView.findViewById(R.id.hrsEditText);
                EditText daysEditText = dialogView.findViewById(R.id.daysEditText);
                EditText weeksEditText = dialogView.findViewById(R.id.weeksEditText);
                hrsCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    hrsEditText.setEnabled(isChecked);
                });
                dayCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    daysEditText.setEnabled(isChecked);
                });
                weeksCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    weeksEditText.setEnabled(isChecked);
                });

                new MaterialAlertDialogBuilder(context)
                        .setTitle("Reschedule task")
                        .setMessage("This task will be rescheduled to a new time. " +
                                "Select the extension period otherwise the task will be rescheduled to the next available time from now." +
                                "Extension period means the task will be scheduled at a free time after the extension period.")
                        .setView(dialogView)
                        .setPositiveButton("RESCHEDULE", (dialog, which) -> {
                            // Handle positive button click
                            // get data
                            int hrs = hrsEditText.getText().toString().isEmpty() ? 0 : Integer.parseInt(hrsEditText.getText().toString());
                            int days = daysEditText.getText().toString().isEmpty() ? 0 : Integer.parseInt(daysEditText.getText().toString());
                            int weeks = weeksEditText.getText().toString().isEmpty() ? 0 : Integer.parseInt(weeksEditText.getText().toString());

                            db.rescheduleTask(task, context, hrs, days, weeks);
                            // notify change
                            setTaskList(db.getTasksOnDate(date));
                            notifyDataSetChanged();
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            // Handle negative button click
                            dialog.cancel();
                        }).show();
            }
        }

        private void saveNotificationPreference(String prefName) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(prefName, true);
            editor.apply();
        }
    }

}

