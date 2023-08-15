package com.example.planify;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class DataViewModel extends ViewModel {
    private MutableLiveData<List<Task>> taskList = new MutableLiveData<>();
    public void setTaskList(List<Task> taskList) {
        this.taskList.setValue(taskList);
    }

    public LiveData<List<Task>> getTaskListLiveData() {
        return taskList;
    }
}
