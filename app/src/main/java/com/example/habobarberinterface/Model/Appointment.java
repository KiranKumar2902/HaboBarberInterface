package com.example.habobarberinterface.Model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.DocumentId;

public class Appointment implements Parcelable {

    @PropertyName("slot")
    private int slot;
    @PropertyName("customerName")
    private String customerName;
    @PropertyName("customerPhone")
    private String customerPhone;
    @PropertyName("time")
    private String time;

    public Appointment(){

    }

    public Appointment(int slot, String customerName, String customerPhone, String time) {
        this.slot = slot;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.time = time;
    }

    protected Appointment(Parcel in) {
        slot = in.readInt();
        customerName = in.readString();
        customerPhone = in.readString();
        time = in.readString();
    }

    public static final Creator<Appointment> CREATOR = new Creator<Appointment>() {
        @Override
        public Appointment createFromParcel(Parcel in) {
            return new Appointment(in);
        }

        @Override
        public Appointment[] newArray(int size) {
            return new Appointment[size];
        }
    };

    @PropertyName("slot")
    public int getSlot() {
        return slot;
    }
    @PropertyName("slot")
    public void setSlot(int slot) {
        this.slot = slot;
    }
    @PropertyName("customerName")
    public String getCustomerName() {
        return customerName;
    }
    @PropertyName("customerName")
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    @PropertyName("customerPhone")
    public String getCustomerPhone() {
        return customerPhone;
    }
    @PropertyName("customerPhone")
    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }
    @PropertyName("time")
    public String getTime() {
        return time;
    }
    @PropertyName("time")
    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(slot);
        parcel.writeString(customerName);
        parcel.writeString(customerPhone);
        parcel.writeString(time);
    }
}
