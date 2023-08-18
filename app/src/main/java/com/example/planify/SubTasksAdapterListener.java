package com.example.planify;

public interface SubTasksAdapterListener {
    void onDataChanged(int taskId);
    void onEntireUpdateRequired();
}