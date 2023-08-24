package com.example.planify;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private DataViewModel dataViewModel;
    private DatabaseHelper db;
    private int counter = 0;


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
                    switchFragment(new CoursesFragment());
            else
                return false;
            return true;
        });

        // Set the initial fragment
        switchFragment(new Dashboard());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.clearDay) {
            // Clear Day
            new MaterialAlertDialogBuilder(MainActivity.this)
                    .setTitle("Clear Day")
                            .setMessage("All tasks will be pushed forward by one day if no time conflict exists.")
                                    .setPositiveButton("PUSH FORWARD", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            db.clearMyDays(new Date().getTime(), new Date().getTime() + (24 * 60 * 60 * 1000));
                                        }
                                    })
                    .setNeutralButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    })
                    .show();
            return true;
        } else if (itemId == R.id.clearWeek){
            // Clear Week
            new MaterialAlertDialogBuilder(MainActivity.this)
                    .setTitle("Clear Week")
                    .setMessage("All tasks will be pushed forward by one week.")
                    .setPositiveButton("PUSH FORWARD", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            db.clearMyDays(new Date().getTime(), new Date().getTime() + (24L * 60 * 60 * 1000 * 7));
                        }
                    })
                    .setNeutralButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    })
                    .show();
            return true;
        } else if (itemId == R.id.clearHrs) {
            View dialogView = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_layout_3, null);
            EditText hrsEditText = dialogView.findViewById(R.id.hrsEditText);
            new MaterialAlertDialogBuilder(MainActivity.this)
                    .setTitle("Clear the next hours")
                    .setMessage("All tasks will be pushed forward by time input below. Forwards by 1 hour by default.")
                    .setView(dialogView)
                    .setPositiveButton("PUSH FORWARD", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            int hrs = hrsEditText.getText().toString().isEmpty() ? 0 : Integer.parseInt(hrsEditText.getText().toString());
                            db.clearMyDays(new Date().getTime(), new Date().getTime() + (hrs * 60L * 60 * 1000));
                        }
                    })
                    .setNeutralButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    })
                    .show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void switchFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    Timer timer = new Timer();
    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            counter = 0;
        }
    };

    @Override
    public void onBackPressed() {
        if (counter == 0){
            Toast.makeText(MainActivity.this, "Press again to exit", Toast.LENGTH_SHORT).show();
            counter++;
            // implement timer
            timer.schedule(timerTask, 5000);
        } else {
            counter = 0;
            finish();
            System.exit(0);
        }
    }
}