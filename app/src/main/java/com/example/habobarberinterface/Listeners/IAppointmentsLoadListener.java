package com.example.habobarberinterface.Listeners;

import com.example.habobarberinterface.Model.Appointment;

import java.util.List;

public interface IAppointmentsLoadListener {

    void onAppointmentsLoadSuccess(List<Appointment> banners);
    void onAppointmentsLoadFailed(String message);
}