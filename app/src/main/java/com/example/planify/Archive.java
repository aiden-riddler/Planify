package com.example.planify;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

        return view;
    }
}