package com.example.planify;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class SubTaskAdapter extends RecyclerView.Adapter<SubTaskAdapter.SubTaskViewHolder> {

    private List<SubTask> subTaskList;
    private SubTasksAdapterListener listener;
    private Context context;

    public SubTaskAdapter(List<SubTask> subTaskList, Context context) {
        this.subTaskList = subTaskList;
        this.context = context;
    }

    @NonNull
    @Override
    public SubTaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subtask, parent, false);
        return new SubTaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubTaskViewHolder holder, int position) {
        SubTask subTask = subTaskList.get(position);
        holder.subTaskNameTextView.setText(subTask.getSubTaskName() + "." + convertSecondsToHMS(subTask.getTimeRequired()));

        if (subTask.isSubTaskCompleted()) {
//            holder.done.setEnabled(false);
            holder.done.setColorFilter(Color.GREEN);
            holder.forward.setEnabled(false);
            holder.forward.setVisibility(View.INVISIBLE);
            holder.forward.setColorFilter(Color.GREEN);
        } else {
            holder.done.setEnabled(true);
            holder.done.setColorFilter(Color.GRAY);
            holder.forward.setEnabled(true);
            holder.forward.setVisibility(View.VISIBLE);
            holder.forward.setColorFilter(Color.BLUE);
        }
    }

    @Override
    public int getItemCount() {
        return subTaskList.size();
    }

    class SubTaskViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView subTaskNameTextView;
        ImageButton done;
        ImageButton forward;

        SubTaskViewHolder(View itemView) {
            super(itemView);
            subTaskNameTextView = itemView.findViewById(R.id.subTaskName);
            forward = itemView.findViewById(R.id.forward);
            forward.setOnClickListener(this);
            done = itemView.findViewById(R.id.done);
            done.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            DatabaseHelper db = new DatabaseHelper(context);
            SubTask subTask = subTaskList.get(position);
            if (view.getId() == done.getId()){
                subTask.setSubTaskCompleted(!subTask.isSubTaskCompleted());
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                boolean disableDialog = preferences.getBoolean("disable_done_dialog", false);
                if (disableDialog){
                    db.updateSubTask(subTask);
                    // notify change
                    listener.onDataChanged(subTask.getParentTaskId());
                    subTaskList.remove(position);
                    subTaskList.add(position, subTask);
                    notifyDataSetChanged();
                } else {
                    View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_layout, null);
                    CheckBox checkBox = dialogView.findViewById(R.id.checkBox);
                    new MaterialAlertDialogBuilder(context)
                        .setTitle("Mark Complete")
                        .setMessage("This task will be marked " + (subTask.isSubTaskCompleted() ? "complete." : "incomplete"))
                        .setView(dialogView)
                        .setPositiveButton((subTask.isSubTaskCompleted() ? "MARK COMPLETE" : "MARK INCOMPLETE"), (dialog, which) -> {
                            // Handle positive button click
                            db.updateSubTask(subTask);
                            // notify change
                            listener.onDataChanged(subTask.getParentTaskId());
                            subTaskList.remove(position);
                            subTaskList.add(position, subTask);
                            notifyDataSetChanged();
                            if (checkBox.isChecked()) {
                                // Save user preference for disabling notifications
                                saveNotificationPreference("disable_done_dialog");
                            }
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            // Handle negative button click
                        }).show();
                }
            } else if (view.getId() == forward.getId()){
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                boolean disableDialog = preferences.getBoolean("disable_forward_subtask_dialog", false);
                if (disableDialog){
                    db.rescheduleSubtask(subTask, context);
                    listener.onEntireUpdateRequired();
                    notifyDataSetChanged();
                } else {
                    View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_layout, null);
                    CheckBox checkBox = dialogView.findViewById(R.id.checkBox);
                    new MaterialAlertDialogBuilder(context)
                            .setTitle("Forward Task")
                            .setMessage("The task " + subTask.getSubTaskName() + " will be rescheduled to the next available time where a similar course exists.")
                            .setView(dialogView)
                            .setPositiveButton("RESCHEDULE", (dialog, which) -> {
                                // Handle positive button click
                                db.rescheduleSubtask(subTask,context);
                                // notify change
                                listener.onEntireUpdateRequired();
                                notifyDataSetChanged();
                                if (checkBox.isChecked()) {
                                    // Save user preference for disabling notifications
                                    saveNotificationPreference("disable_forward_subtask_dialog");
                                }
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> {
                                // Handle negative button click
                                dialog.cancel();
                            }).show();
                }
            }
        }
    }

    public void setListener(SubTasksAdapterListener listener) {
        this.listener = listener;
    }

    private void saveNotificationPreference(String prefName) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(prefName, true);
        editor.apply();
    }

    private String convertSecondsToHMS(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds);
    }
}


