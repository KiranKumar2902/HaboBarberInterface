package com.example.habobarberinterface.Model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.PropertyName;

public class Salon implements Parcelable {
    @PropertyName("Name")
    private String name;
    private String address,salonID;

    public Salon() {
    }

    @PropertyName("Name")
    public String getName() {
        return name;
    }
    @PropertyName("Name")
    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSalonID() {
        return salonID;
    }

    public void setSalonID(String salonID) {
        this.salonID = salonID;
    }

    protected Salon(Parcel in) {
        name = in.readString();
        address = in.readString();
        salonID = in.readString();
    }

    public static final Creator<Salon> CREATOR = new Creator<Salon>() {
        @Override
        public Salon createFromParcel(Parcel in) {
            return new Salon(in);
        }

        @Override
        public Salon[] newArray(int size) {
            return new Salon[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(address);
        parcel.writeString(salonID);
    }
}
