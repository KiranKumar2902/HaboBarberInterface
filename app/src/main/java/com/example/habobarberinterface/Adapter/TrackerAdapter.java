package com.example.habobarberinterface.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habobarberinterface.Model.Appointment;
import com.example.habobarberinterface.R;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class TrackerAdapter extends RecyclerView.Adapter<TrackerAdapter.MyViewHolder>{

    Context context;
    List<Appointment> appointments;

    public TrackerAdapter(Context context, List<Appointment> shops) {
        this.context = context;
        this.appointments = shops;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int item) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.layout_appointment_info,parent,false);
        return new MyViewHolder(itemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackerAdapter.MyViewHolder holder, int position) {
        Appointment mCurrent = appointments.get(position);

        String name = mCurrent.getCustomerName();
        String time = mCurrent.getTime();
        String mobile = mCurrent.getCustomerPhone();

        holder.name.setText(name);
        holder.time.setText(time);
        holder.mobile.setText(mobile);
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        public final TextView name;
        public final TextView time;
        public final TextView mobile;
        final TrackerAdapter mAdapter;

        public MyViewHolder(@NonNull View itemView, TrackerAdapter adapter) {
            super(itemView);
            name = itemView.findViewById(R.id.trackerName);
            time = itemView.findViewById(R.id.trackerTime);
            mobile = itemView.findViewById(R.id.trackerMobile);
            this.mAdapter = adapter;
        }
    }
}


