package com.example.planify;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
public class CoursesFragment extends Fragment {

    private CourseAdapter courseAdapter;
    private DatabaseHelper db;
    public CoursesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Initialize db
        db = new DatabaseHelper(getContext());
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_course, container, false);
        RecyclerView coursesRecycler = view.findViewById(R.id.coursesRecycler);
        coursesRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        courseAdapter = new CourseAdapter(db.getCourses(), getContext());
        coursesRecycler.setAdapter(courseAdapter);

        return view;
    }
}