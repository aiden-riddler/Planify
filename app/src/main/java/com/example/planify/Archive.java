package com.example.planify;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class Archive extends Fragment {

    private RecyclerView nonSchedulableHrsRecycler;
    public Archive() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_archive, container, false);
        nonSchedulableHrsRecycler = view.findViewById(R.id.nonSchedulableHrsRecycler);
        DatabaseHelper db = new DatabaseHelper(getContext());
        NonSchedulerAdapter nonScheduleAdapter = new NonSchedulerAdapter(db.getNonSchedulableHours(), getContext());
        nonSchedulableHrsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        nonSchedulableHrsRecycler.setAdapter(nonScheduleAdapter);

        FloatingActionButton addTaskFab = view.findViewById(R.id.addTaskFab);
        addTaskFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), NonSchedulerActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }
}