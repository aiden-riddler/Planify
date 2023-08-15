package com.example.planify;

import android.os.Bundle;
import android.util.Log;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private DataViewModel dataViewModel;
    private DatabaseHelper db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize DB
        db = new DatabaseHelper(this);
        for (Task task:db.getAllTasks()){
            Log.d("Planify", task.toString());
            Log.d("Planify", "Subtask Size: " + task.getSubTasks().size());
        }
        // Initialize the ViewModel
        dataViewModel = new ViewModelProvider(this).get(DataViewModel.class);
        List<Task> tasksOnDate = db.getTasksOnDate(new Date());
        Log.d("Planify", "Tasks on Date below ");
        for (Task task:tasksOnDate){
            Log.d("Planify", task.toString());
            Log.d("Planify", "Subtask Size: " + task.getSubTasks().size());
        }
        dataViewModel.setTaskList(tasksOnDate);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (R.id.dashboard == item.getItemId())
                    switchFragment(new Dashboard());
            else if (R.id.archive == item.getItemId())
                    switchFragment(new Archive());
            else if (R.id.account == item.getItemId())
                    switchFragment(new Dashboard());
            else
                return false;
            return true;
        });

        // Set the initial fragment
        switchFragment(new Dashboard());
    }

    private void switchFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

}