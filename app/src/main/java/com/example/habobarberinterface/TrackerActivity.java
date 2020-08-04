package com.example.habobarberinterface;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.example.habobarberinterface.Adapter.TrackerAdapter;
import com.example.habobarberinterface.Model.Appointment;

import java.util.List;

public class TrackerActivity extends AppCompatActivity {

    private RecyclerView trackerList;
    private TrackerAdapter searchAdapter;
    private List<Appointment> appointments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);

        //get list
        Intent intent = getIntent();
        Bundle extra = intent.getExtras();
        appointments = extra.getParcelableArrayList("list");

        //get recyclerview widget
        trackerList = findViewById(R.id.appointmentsList);

        //create adapted linked to list
        searchAdapter = new TrackerAdapter(this, appointments);

        //set adapter to recycler view
        trackerList.setAdapter(searchAdapter);

        //set defauly layout manager
        trackerList.setLayoutManager(new LinearLayoutManager(this));
    }
}