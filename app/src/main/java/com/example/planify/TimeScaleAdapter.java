package com.example.planify;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class TimeScaleAdapter extends RecyclerView.Adapter<TimeScaleAdapter.TimeViewHolder> {

    private List<TimeFrame> timeList = new ArrayList<>();

    public void setTimeList(List<TimeFrame> timeList) {
        this.timeList = timeList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_time_scale, parent, false);
        return new TimeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeViewHolder holder, int position) {
        String time = timeList.get(position).getStringTime();
        holder.timeTextView.setText(time);
        ViewGroup.LayoutParams layoutParams = holder.parent.getLayoutParams();
        layoutParams.height = timeList.get(position).getHeight();
        holder.parent.setLayoutParams(layoutParams);
    }

    @Override
    public int getItemCount() {
        return timeList.size();
    }

    static class TimeViewHolder extends RecyclerView.ViewHolder {
        TextView timeTextView;
        ConstraintLayout parent;
        TimeViewHolder(@NonNull View itemView) {
            super(itemView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            parent = itemView.findViewById(R.id.timeHolder);
        }
    }

    public List<TimeFrame> getTimeList() {
        return timeList;
    }
}
