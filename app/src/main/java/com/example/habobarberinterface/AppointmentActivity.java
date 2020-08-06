package com.example.habobarberinterface;

import android.app.AlertDialog;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.habobarberinterface.Common.Common;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import dmax.dialog.SpotsDialog;

public class AppointmentActivity extends AppCompatActivity {

    public int slot = 99;
    DocumentReference slotRef;

    TextView tCustomerName;
    TextView tCustomerMobile;
    TextView tLocation;
    TextView tBookingTime;
    TextView tBarbershop;
    ProgressBar pBar;

    AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment);




        tBarbershop = findViewById(R.id.tBarbershop);
        tCustomerMobile = findViewById(R.id.tCustomerMobile);
        tLocation = findViewById(R.id.tLocation);
        tCustomerName = findViewById(R.id.tCustomerName);
        tBookingTime = findViewById(R.id.tBookingTime);
        pBar = findViewById(R.id.progressBar);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        //Long selectedSlot = extras.getLong("slot");

        if(extras !=null){
            slot = extras.getInt("slot");
        }else{slot = 19;}


        slotRef = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.state_name)
                .collection("Branch")
                .document(Common.selected_salon.getSalonID())
                .collection("Barber")
                .document(Common.currentBarber.getBarberId())
                .collection(Common.simpleDateFormat.format(Common.bookingDate.getTime()))
                .document(Long.toString(slot));
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).setTheme(R.style.Custom).build();

                loadSlot();


    }

    public void loadSlot(){
        slotRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){

                            String customerName = documentSnapshot.getString("customerName");

                            String customerPhone = documentSnapshot.getString("customerPhone");
                            String time = documentSnapshot.getString("time");


                            /*
                            boolean completed = documentSnapshot.getBoolean("done");
                            if(completed){
                                tCompleted.setText("Completed: Yes");
                            }else{tCompleted.setText("Completed: No");}
                            */

                            pBar.setVisibility(View.GONE);
                            tCustomerMobile.setText("Mobile Number: " + customerPhone);
                            tCustomerName.setText("Customer Name: " + customerName);
                            tLocation.setText("Location: " + Common.state_name);
                            tBarbershop.setText("Barbershop: " + Common.selected_salon.getName());
                            tBookingTime.setText(Common.convertTimeSlotToString(slot));

                        }else{
                            System.out.print("Document does not exist");
                            tBookingTime.setText("No Booking Information found");
                            tCustomerName.setText(Common.simpleDateFormat.format(Common.bookingDate.getTime()));
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

    }
}