package com.example.planify;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    private List<Course> courseList;
    private Context context;

    public CourseAdapter(List<Course> courseList, Context context) {
        this.courseList = courseList;
        this.context = context;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = courseList.get(position);
        holder.courseName.setText(course.getCourseName());
        holder.startDate.setText(formatTime(course.getStartDate()));
        holder.endDate.setText(formatTime(course.getEndDate()));
        holder.progressBar.setProgress((int)((course.getCompleteSubTasksCount()/course.getSubTasksCount()) * 100));
    }

    private String formatTime(long timeInMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat(" MMMM yyyy", Locale.ENGLISH);
        String formattedDate = formatDate(timeInMillis, sdf);
        return formattedDate;
    }

    public static String formatDate(long timestamp, SimpleDateFormat sdf) {
        Date date = new Date(timestamp);
        String dayOfMonth = new SimpleDateFormat("d", Locale.ENGLISH).format(date);
        String formattedDate = sdf.format(date);

        // Append "st", "nd", "rd", or "th" to the day of the month
        if (dayOfMonth.equals("1") || dayOfMonth.equals("21") || dayOfMonth.equals("31")) {
            dayOfMonth += "st";
        } else if (dayOfMonth.equals("2") || dayOfMonth.equals("22")) {
            dayOfMonth += "nd";
        } else if (dayOfMonth.equals("3") || dayOfMonth.equals("23")) {
            dayOfMonth += "rd";
        } else {
            dayOfMonth += "th";
        }

        return dayOfMonth + formattedDate;
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    class CourseViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView courseName;
        TextView startDate;
        TextView endDate;
        ProgressBar progressBar;
        ImageButton add;
        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            courseName = itemView.findViewById(R.id.courseTextView);
            startDate = itemView.findViewById(R.id.startDate);
            endDate = itemView.findViewById(R.id.endDate);
            progressBar = itemView.findViewById(R.id.progressBar);
            add = itemView.findViewById(R.id.add);
        }

        @Override
        public void onClick(View view) {
            Course course = courseList.get(getAdapterPosition());
            if (view.getId() == add.getId()){
                new MaterialAlertDialogBuilder(context)
                        .setTitle("Add Course Task")
                        .setMessage("Add task/activity to " + course.getCourseName())
                        .setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(context, CourseTaskActivity.class);
                                intent.putExtra("Course", course);
                                context.startActivity(intent);
                            }
                        })
                        .setNeutralButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        }).show();
            }
        }
    }
}


